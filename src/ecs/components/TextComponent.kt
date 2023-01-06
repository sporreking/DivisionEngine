package ecs.components

import com.curiouscreature.kotlin.math.Float2
import ecs.Component
import resources.AttributeData
import resources.Font
import resources.Mesh
import resources.ShaderProgram
import ecs.systems.TextRenderSystem

class TextComponent(
    /** The font to use for this component. */
    val font: Font,

    /** The text of this component. */
    text: String,

    /** Whether the text should be vertically centered around the transform's origin. */
    val centerVertical: Boolean = false,

    /** Whether the text should be horizontally centered around the transform's origin. */
    val centerHorizontal: Boolean = false,

    /**
     * Whether the origin of the text should be at the bottom of the first line, or at the top.
     * Note that if [centerVertical] is set to true, this value has no effect.
     */
    val bottomAnchor: Boolean = false,

    /**
     * The number of characters that should be allowed to be contained by the [mesh] before it's size has to be
     * increased. Set to null if the buffer length should be fitted  tothe specified [text].
     */
    meshBufferCapacity: Int? = null
) : Component() {

    /** The number of characters that are currently stored in the [mesh]. */
    var meshBufferLength = 0

    /**
     * The maximum number of characters that fit into the current mesh. If the [text] is manipulated such that the
     * number of characters that require representation in the mesh exceeds the current capacity, new memory must be
     * allocated on the GPU, and this value should be updated. Note that [TextRenderSystem] does this
     * on its update pass.
     */
    var meshBufferCapacity = meshBufferCapacity ?: Font.numMeshCharacters(text)

    /** The mesh to use for the [text]. */
    val mesh = Mesh(listOf(
        AttributeData<Float2>(ShaderProgram.ATTLOC_POSITION, emptyList(), true),
        AttributeData<Float2>(ShaderProgram.ATTLOC_TEX_COORDS, emptyList(), true)
    ), emptyList(), true)

    /** True if the [mesh] must be updated to reflect the current [text]. */
    var needsUpdate = true

    /** The text of this component. */
    var text: String = text
        set(value) { needsUpdate = true; field = value }
}