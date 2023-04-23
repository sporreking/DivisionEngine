package resources

import com.curiouscreature.kotlin.math.Float2
import kotlin.math.ceil

/** Describes the properties of a literal character. */
data class FontChar(
    val id: Char,
    val textureCoordinates: List<Float2>,
    val size: Float2,
    val offset: Float2,
    val advance: Float
) {
    companion object {
        const val VERTEX_POSITION_SIZE = 3
    }
}

/** Describes a font and all of its properties. */
data class Font(
    /** The name of the font. */
    val name: String,

    /** The font map. */
    val texture: Texture,

    /** The characters of this font. */
    val characters: Map<Char, FontChar>,

    /** The line height of this font. */
    val lineHeight: Float,

    /** the base height of this font. */
    val base: Float,

    /** The padding of this font. */
    val padding: List<Float>
) {
    val paddingTop get() = padding[0]
    val paddingLeft get() = padding[1]
    val paddingBottom get() = padding[2]
    val paddingRight get() = padding[3]

    companion object {
        /**
         * Counts the number of characters of the given [text] that require storage in a mesh.
         * Characters that do not require storage are blank spaces and newline characters etc.
         */
        fun numMeshCharacters(text: String) = text.length - text.count { c -> c == ' ' || c == '\n' }
    }

    /** Calculates the width of the given [text] as if it was rendered using this font. */
    fun getTextWidth(text: String) = text.split("\n").maxOf { line ->
        line.sumOf { c ->
            ((characters[c] ?: characters[Char(0)])?.advance ?: 0).toDouble()
        }.toFloat()
    }

    // TODO: IMPLEMENT
    fun getTextHeight(text: String, maxWidth: Float) = 0f
}