package resources

import com.curiouscreature.kotlin.math.Float3
import org.lwjgl.openal.AL10.*
import org.lwjgl.system.MemoryStack

class AudioSource {

    val handle = alGenSources()

    init { alSourcei(handle, AL_SOURCE_RELATIVE, AL_TRUE) }

    var pitch get() = alGetSourcef(handle, AL_PITCH); set(pitch) { alSourcef(handle, AL_PITCH, pitch) }

    var position get() = MemoryStack.stackPush().let { stack ->
        val x = stack.mallocFloat(1); val y = stack.mallocFloat(1); val z = stack.mallocFloat(1)

        alGetSource3f(handle, AL_POSITION, x, y, z)

        stack.pop()

        Float3(x.get(), y.get(), z.get())
    }; set(velocity) = alSource3f(handle, AL_POSITION, velocity.x, velocity.y, velocity.z)

    var velocity get() = MemoryStack.stackPush().let { stack ->
        val x = stack.mallocFloat(1); val y = stack.mallocFloat(1); val z = stack.mallocFloat(1)

        alGetSource3f(handle, AL_VELOCITY, x, y, z)

        stack.pop()

        Float3(x.get(), y.get(), z.get())
    }; set(velocity) = alSource3f(handle, AL_VELOCITY, velocity.x, velocity.y, velocity.z)

    var gain get() = alGetSourcef(handle, AL_GAIN); set(gain) { alSourcef(handle, AL_GAIN, gain) }

    fun play(clip: AudioClip, loop: Boolean = false, relative: Boolean = false) {
        stop()
        alSourcei(handle, AL_LOOPING, if (loop) AL_TRUE else AL_FALSE)
        alSourcei(handle, AL_SOURCE_RELATIVE, if (relative) AL_TRUE else AL_FALSE)
        alSourcei(handle, AL_BUFFER, clip.handle)
        alSourcePlay(handle);
    }

    fun isPlaying() = alGetSourcei(handle, AL_SOURCE_STATE) == AL_PLAYING

    fun pause() = alSourcePause(handle)

    fun stop() = alSourceStop(handle)
}