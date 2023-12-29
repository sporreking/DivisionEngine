package resources

import org.lwjgl.opengl.GL13.*
import org.lwjgl.stb.STBImage
import java.nio.ByteBuffer

data class TextureLoadInstruction(val path: String)

/** Represents a texture. */
class Texture(
    /** The texture data to send to the GPU. */
    data: ByteBuffer,
    /** The width of the texture. */
    val width: Int,
    /** The height of the texture. */
    val height: Int
) : Resource<TextureLoadInstruction>() {

    /** The OpenGL handle of this texture. */
    val handle = glGenTextures()

    init {
        // Load texture
        glBindTexture(GL_TEXTURE_2D, handle)
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, data)

        // Set texture parameters
        setMinMagFilter(GL_NEAREST)
        setWrap(GL_CLAMP_TO_EDGE)

        // Unbind texture
        glBindTexture(GL_TEXTURE_2D, 0)
    }

    /**
     * Sets what [filterFunction] to use for up and down sampling. Some valid values are `GL_NEAREST` and `GL_LINEAR`.
     */
    fun setMinMagFilter(filterFunction: Int) {
        glBindTexture(GL_TEXTURE_2D, handle)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, filterFunction)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, filterFunction)
    }

    /**
     * Sets what [wrapFunction] to use when texture coordinates lie outside of range [0, 1]. Some valid values
     * are `GL_CLAMP_TO_EDGE` and `GL_REPEAT`.
     */
    fun setWrap(wrapFunction: Int) {
        glBindTexture(GL_TEXTURE_2D, handle)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, wrapFunction)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, wrapFunction)
    }

    /** Binds this texture to the specified slot. */
    fun bind(slot: Int = 0) {
        glActiveTexture(GL_TEXTURE0 + slot)
        glBindTexture(GL_TEXTURE_2D, handle)
    }

    override fun load(loadInstruction: TextureLoadInstruction) {
        // Allocate information slots
        val w = IntArray(1)
        val h = IntArray(1)
        val comp = IntArray(1)

        // Load image onto heap
        STBImage.stbi_set_flip_vertically_on_load(true)
        val data = STBImage.stbi_load(loadInstruction.path, w, h, comp, 4) ?: return null

        // Generate texture and store in registry
        val t = Texture(data, w[0], h[0])
        set(name, t)

        // Free image from heap
        STBImage.stbi_image_free(data)

        return t
    }
}