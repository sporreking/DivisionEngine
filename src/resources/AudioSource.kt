package resources

import com.curiouscreature.kotlin.math.Float3
import org.lwjgl.openal.AL10.*
import org.lwjgl.system.MemoryStack

/** Represents an audio source and all of its properties. */
class AudioSource {

    /** The OpenAL handle of the audio source. */
    val handle = alGenSources()

    init { alSourcei(handle, AL_SOURCE_RELATIVE, AL_TRUE) }

    /** The pitch of the audio source. */
    var pitch get() = alGetSourcef(handle, AL_PITCH); set(pitch) { alSourcef(handle, AL_PITCH, pitch) }

    /** The position of the audio source. */
    var position get() = MemoryStack.stackPush().let { stack ->
        val x = stack.mallocFloat(1); val y = stack.mallocFloat(1); val z = stack.mallocFloat(1)

        alGetSource3f(handle, AL_POSITION, x, y, z)

        stack.pop()

        Float3(x.get(), y.get(), z.get())
    }; set(velocity) = alSource3f(handle, AL_POSITION, velocity.x, velocity.y, velocity.z)

    /** The velocity of the audio source. */
    var velocity get() = MemoryStack.stackPush().let { stack ->
        val x = stack.mallocFloat(1); val y = stack.mallocFloat(1); val z = stack.mallocFloat(1)

        alGetSource3f(handle, AL_VELOCITY, x, y, z)

        stack.pop()

        Float3(x.get(), y.get(), z.get())
    }; set(velocity) = alSource3f(handle, AL_VELOCITY, velocity.x, velocity.y, velocity.z)

    /** The gain of the audio source. */
    var gain get() = alGetSourcef(handle, AL_GAIN); set(gain) { alSourcef(handle, AL_GAIN, gain) }

    /**
     * Plays the specified [clip]. If the audio clip should be looped, the [loop] parameter should be set to `true`.
     * Also, if the clip should be played relative to the audio listener rather than at its absolute position,
     * the [relative] parameter should be set to `true`.
     */
    fun play(clip: AudioClip, loop: Boolean = false, relative: Boolean = false) {
        stop()
        alSourcei(handle, AL_LOOPING, if (loop) AL_TRUE else AL_FALSE)
        alSourcei(handle, AL_SOURCE_RELATIVE, if (relative) AL_TRUE else AL_FALSE)
        alSourcei(handle, AL_BUFFER, clip.handle)
        alSourcePlay(handle);
    }

    /** Returns `true` if an [AudioClip] is currently being played by this audio source. */
    fun isPlaying() = alGetSourcei(handle, AL_SOURCE_STATE) == AL_PLAYING

    /** Pauses the audio source. */
    fun pause() = alSourcePause(handle)

    /** Stops the audio source. */
    fun stop() = alSourceStop(handle)
}