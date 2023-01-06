package resources

import org.lwjgl.BufferUtils
import org.lwjgl.stb.STBVorbis
import org.lwjgl.stb.STBVorbisInfo
import org.lwjgl.system.MemoryStack.stackPush
import org.lwjgl.system.MemoryUtil.NULL
import java.io.File
import java.nio.ByteBuffer

class AudioClipRegistry : Registry<AudioClip, String>() {
    override fun load(name: String, loadInstruction: String) = set(
        name,
        loadInstruction.let { path ->
            var clip: AudioClip? = null
            stackPush().also { stack ->
                // Read file to bytebuffer
                val vorbis = File(path).readBytes().let { data ->
                    BufferUtils.createByteBuffer(data.size).put(data).also { b -> b.flip() }
                }

                // Allocate memory for error
                val error = stack.mallocInt(1)

                // Open decoder
                val decoder = STBVorbis.stb_vorbis_open_memory(vorbis, error, null)
                check(decoder != NULL) { "Failed to open Ogg Vorbis file. Error: ${error.get(0)}" }

                // Extract info
                val info = STBVorbisInfo.create()
                STBVorbis.stb_vorbis_get_info(decoder, info)

                val channels = info.channels()
                val lengthSamples = STBVorbis.stb_vorbis_stream_length_in_samples(decoder)

                // Decode data
                val pcm = BufferUtils.createShortBuffer(lengthSamples)
                STBVorbis.stb_vorbis_get_samples_short_interleaved(decoder, channels, pcm)

                // Release decoder
                STBVorbis.stb_vorbis_close(decoder)

                // Create audio clip
                clip = AudioClip(pcm, info)
            }.pop()

            return@let clip
        } ?: throw IllegalStateException("Could not load audio clip: $loadInstruction")
    )
}