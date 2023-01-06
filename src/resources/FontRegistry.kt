package resources

import com.curiouscreature.kotlin.math.Float2
import org.lwjgl.opengl.GL11.GL_LINEAR
import java.io.File

data class FontLoadInstruction(
    /** A path leading to a `.fnt` file to load. */
    val path: String,

    /** The desired padding in pixels. */
    val desiredPadding: Int
)

class FontRegistry : Registry<Font, FontLoadInstruction>() {
    override fun load(name: String, loadInstruction: FontLoadInstruction) = set(
        name,
        loadInstruction.path.let { path ->
            // Variables for tracking file contents
            var realName: String? = null
            var texturePath: String? = null
            var textureWidth: Int? = null
            var textureHeight: Int? = null
            var lineHeight: Int? = null
            var base: Int? = null
            var padding = listOf(0f, 0f, 0f, 0f)


            // Create font characters
            val fontChars = buildMap {
                File(path).forEachLine { line ->

                    // Decompose line
                    val split = line.trim().replace(" +".toRegex(), " ").split(" ")
                    val keyword = split.firstOrNull() ?: return@forEachLine
                    val args = split.drop(1).associate { item ->
                        item.split("=").let {
                            it[0] to it[1].replace("\"", "")
                        }
                    }


                    // Handle line based on keyword
                    when (keyword) {
                        "info" -> {
                            realName = args["face"]!!
                            padding = args["padding"]!!.split(",").map { it.toFloat() }
                        }
                        "page" -> texturePath = File(File(path).parentFile!!, args["file"]!!).toString()
                        "common" -> {
                            textureWidth = args["scaleW"]!!.toInt(); textureHeight = args["scaleH"]!!.toInt()
                            lineHeight = args["lineHeight"]!!.toInt(); base = args["base"]!!.toInt()
                           padding = padding.mapIndexed { i, p ->
                                p / if (i % 2 == 1) textureWidth!! else textureHeight!!
                            }
                        }
                        "char" -> {
                            checkNotNull(textureWidth) { "Texture width not found!" }
                            checkNotNull(textureHeight) { "Texture height not found!" }

                            // Extract variables
                            val char = args["id"]!!.toInt().toChar()
                            val pT = padding[0] // Padding top
                            val pL = padding[1] // Padding left
                            val pB = padding[2] // Padding bottom
                            val pR = padding[3] // Padding right
                            val pW = pL + pR // Padding width
                            val pH = pT + pB // Padding height
                            val pDW = loadInstruction.desiredPadding.toFloat() / textureWidth!!
                            val pDH = loadInstruction.desiredPadding.toFloat() / textureHeight!!
                            val x = args["x"]!!.toFloat() / textureWidth!! + pL - pDW
                            val y = args["y"]!!.toFloat() / textureHeight!! + pT - pDH
                            val w = (args["width"]!!.toFloat() / textureHeight!! - pW + 2 * pDW)
                            val h = (args["height"]!!.toFloat() / textureHeight!! - pH + 2 * pDH)
                            val offset = Float2(
                                args["xoffset"]!!.toFloat() / textureWidth!! + pL - pDW,
                                -(args["yoffset"]!!.toFloat() / textureHeight!! + pT - pDH)
                            )
                            val advance = (args["xadvance"]!!.toFloat() / textureWidth!! - pW)

                            // Create FontChar
                            set(
                                char, FontChar(
                                    char, listOf(
                                        Float2(x, 1f - (y + h)), // Bottom left
                                        Float2(x, 1f - y), // Top left
                                        Float2(x + w, 1f - y), // Top right
                                        Float2(x + w, 1f - (y + h)) // Bottom right
                                    ), Float2(w, h), offset, advance
                                )
                            )
                        }
                    }
                }
            }

            // Sanity checks
            checkNotNull(realName) { "No font name was found!" }
            checkNotNull(texturePath) { "No font texture path was found!" }
            check(File(texturePath!!).exists()) { "Texture $texturePath does not exist!" }
            checkNotNull(lineHeight) { "No line height was found!" }
            checkNotNull(base) { "No line base was found!" }

            // Construct and return actual font instance
            return@let Font(
                realName!!,
                TextureRegistry().load(name, texturePath!!)!!.also { t ->
                    t.setMinMagFilter(GL_LINEAR)
                },
                fontChars,
                lineHeight!!.toFloat() / textureHeight!!,
                base!!.toFloat() / textureHeight!!,
                padding
            )
        }
    )
}