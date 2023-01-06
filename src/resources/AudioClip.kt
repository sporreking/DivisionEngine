package resources

import org.lwjgl.openal.AL10.*
import org.lwjgl.stb.STBVorbisInfo
import java.nio.ShortBuffer

class AudioClip(
    /** The data to use for this audio clip. */
    pcm: ShortBuffer,

    /** Information about the audio clip. */
    val info: STBVorbisInfo
) {

    /** The OpenAL handle of this audio clip. */
    val handle = alGenBuffers()

    init {
        alBufferData(
            handle,
            if (info.channels() == 1) AL_FORMAT_MONO16 else AL_FORMAT_STEREO16,
            pcm,
            info.sample_rate()
        )
        println(info.channels())
    }
}