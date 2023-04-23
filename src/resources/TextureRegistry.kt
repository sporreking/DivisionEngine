package resources

import org.lwjgl.stb.STBImage

/** Keeps track of labeled [Textures][Texture]. */
class TextureRegistry : Registry<Texture, String>() {

    /**
     * Loads a resources using the system file path described by [loadInstruction],
     * and stores it at the specified [name].
     */
    override fun load(name: String, loadInstruction: String): Texture? {

        // Allocate information slots
        val w = IntArray(1)
        val h = IntArray(1)
        val comp = IntArray(1)

        // Load image onto heap
        STBImage.stbi_set_flip_vertically_on_load(true)
        val data = STBImage.stbi_load(loadInstruction, w, h, comp, 4) ?: return null

        // Generate texture and store in registry
        val t = Texture(data, w[0], h[0])
        set(name, t)

        // Free image from heap
        STBImage.stbi_image_free(data)

        return t
    }
}