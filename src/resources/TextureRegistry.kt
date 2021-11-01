package resources

import org.lwjgl.stb.STBImage

class TextureRegistry : Registry<Texture>() {

    override fun load(name: String, path: String) {

        // Allocate information slots
        val w = IntArray(1)
        val h = IntArray(1)
        val comp = IntArray(1)

        // Load image onto heap
        STBImage.stbi_set_flip_vertically_on_load(true)
        val data = STBImage.stbi_load(path, w, h, comp, 4) ?: return

        // Generate texture and store in registry
        set(name, Texture(data, w[0], h[0]))

        // Free image from heap
        STBImage.stbi_image_free(data)
    }
}