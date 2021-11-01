package resources

import org.lwjgl.opengl.GL13.*
import java.nio.ByteBuffer

class Texture(
    data: ByteBuffer,
    val width: Int,
    val height: Int
) {

    // Generate texture
    val handle = glGenTextures()

    init {
        // Load texture
        glBindTexture(GL_TEXTURE_2D, handle)
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, data)

        // Set texture parameters
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE)

        // Unbind texture
        glBindTexture(GL_TEXTURE_2D, 0)
    }

    /** Binds this texture to the specified slot. */
    fun bind(slot: Int = 0) {
        glActiveTexture(GL_TEXTURE0 + slot)
        glBindTexture(GL_TEXTURE_2D, handle)
    }
}