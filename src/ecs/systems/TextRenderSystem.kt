package ecs.systems

import com.curiouscreature.kotlin.math.Float2
import com.curiouscreature.kotlin.math.Float3
import ecs.Camera
import ecs.ECSystem
import ecs.Scene
import ecs.components.TextComponent
import io.InputManager
import org.lwjgl.opengl.GL11.GL_UNSIGNED_INT
import org.lwjgl.opengl.GL11.glDrawElements
import resources.*

/**
 * Handles [TextComponents][TextComponent] such that their [meshes][TextComponent.mesh] are updated, and renders them.
 */
class TextRenderSystem : ECSystem() {
    override fun update(scene: Scene, inputManager: InputManager, delta: Double) {
        scene.getComponents<TextComponent>().values.forEach { tc ->
            if (!tc.needsUpdate) return@forEach

            if (Font.numMeshCharacters(tc.text) == 0) { tc.meshBufferLength = 0; return@forEach }

            checkNotNull(tc.mesh.indexBufferObject) { "There is no IBO!" }

            checkNotNull(tc.mesh.vertexBufferObjects[ShaderProgram.ATTLOC_POSITION]) {
                "There is no VBO for the position attribute!"
            }

            checkNotNull(tc.mesh.vertexBufferObjects[ShaderProgram.ATTLOC_TEX_COORDS]) {
                "There is no VBO for the texture coordinates attribute!"
            }

            // Get position VBO
            val positionVBO = tc.mesh.vertexBufferObjects[ShaderProgram.ATTLOC_POSITION]!!

            // Save old buffer size
            val oldSize = positionVBO.size

            // Update mesh positions
            tc.mesh.replaceAttributeData(AttributeData(
                ShaderProgram.ATTLOC_POSITION,
                tc.text.split("\n").flatMapIndexed { lineIndex, line ->
                    var advance = 0f
                    line.flatMap { c -> // Handle each char in current line
                        (tc.font.characters[c] ?: tc.font.characters[Char(0)])?.let { fc ->
                            (if (c == ' ') emptyList() else listOf(
                                Float3(advance, -fc.size.y, 0f),
                                Float3(advance, 0f, 0f),
                                Float3(advance + fc.size.x, 0f, 0f),
                                Float3(advance + fc.size.x, -fc.size.y, 0f)
                            )).map { v -> // Add offsets to char
                                v + fc.offset + Float2(
                                    0f,
                                    (if (tc.bottomAnchor) tc.font.base else 0f) - lineIndex * tc.font.lineHeight
                                )
                            }.also { advance += fc.advance }
                        } ?: emptyList()
                    }.map { v -> // Center line
                        v + Float3(
                            if (tc.centerHorizontal) -tc.font.getTextWidth(line) / 2 else 0f,
                            if (tc.centerVertical) (
                                if (tc.bottomAnchor)
                                    (tc.text.split("\n").size - 1) * tc.font.lineHeight - tc.font.base
                                else
                                    (tc.text.split("\n").size) * tc.font.lineHeight - tc.font.base
                            ) / 2 else 0f,
                            0f
                        )
                    }
                }, true
            ))

            // Update mesh texture coordinates
            tc.mesh.replaceAttributeData(AttributeData(
                ShaderProgram.ATTLOC_TEX_COORDS,
                tc.text.flatMap { c ->
                    if (c == '\n' || c == ' ') return@flatMap emptyList()
                    (tc.font.characters[c] ?: tc.font.characters[Char(0)])?.textureCoordinates ?: emptyList()
                }
            ))

            // Update buffer info
            with(tc) {
                // There are 4 vertices in a single character quad
                val verticesPerQuad = 4

                // Derive number of characters from mesh
                meshBufferLength = positionVBO.size / (FontChar.VERTEX_POSITION_SIZE * verticesPerQuad)
                meshBufferCapacity = positionVBO.capacity / (FontChar.VERTEX_POSITION_SIZE * verticesPerQuad)
            }

            // Update indices if VBO size changed
            if (oldSize != positionVBO.size) {
                tc.mesh.indexBufferObject.replace((0 until tc.meshBufferLength)
                    .flatMap { i -> listOf(
                        i * 4 + 0, i * 4 + 1, i * 4 + 2,
                        i * 4 + 0, i * 4 + 2, i * 4 + 3
                    )}.toIntArray())
            }

            tc.needsUpdate = false
        }
    }

    override fun render(scene: Scene) {
        scene.getComponents<TextComponent>().values.forEach { tc ->

            // Do not render if there is no text
            if (tc.meshBufferLength == 0) return@forEach

            // Fetch font shader program
            val shaderProgram = ShaderProgram.DEFAULT[ShaderProgramRegistry.FONT_NAME]!!

            // Send camera
            shaderProgram.sendCamera(scene.camera!!)

            // Send model transform
            shaderProgram.sendModelTransform(tc.parent!!.transform, bind = false)

            // Send material and bind shader program
            shaderProgram.sendMaterial(Material(texture = tc.font.texture))

            // Bind mesh
            tc.mesh.bind()

            // Draw mesh
            glDrawElements(tc.mesh.mode, tc.mesh.numIndices, GL_UNSIGNED_INT, 0)
        }
    }
}