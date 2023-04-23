package ecs.systems

import ecs.ECSystem
import ecs.Scene
import ecs.components.AudioListenerComponent
import ecs.components.AudioSourceComponent
import io.InputManager
import io.Logger
import org.lwjgl.openal.AL10.*

/**
 * Update all [audio sources][AudioSourceComponent] within the scene, and the [AudioListenerComponent] if it exists.
 * Note that there should only be one audio listener within each scene.
 */
class SimpleAudioSystem : ECSystem() {
    override fun update(scene: Scene, inputManager: InputManager, delta: Double) {
        // Update listener
        scene.getComponents<AudioListenerComponent>().values.also { als ->
            if (als.size > 1) Logger.warn("There is more than one audio listener! (currently: ${als.size})")
        }.firstOrNull()?.let { listener ->
            alListener3f(AL_POSITION, listener.transform!!.px, listener.transform!!.py, listener.transform!!.pz)
            alListener3f(AL_VELOCITY, 0f, 0f, 0f)
            alListenerfv(AL_ORIENTATION, floatArrayOf(
                *with(listener.transform!!.forward) { floatArrayOf(x, y, z) },
                *with(listener.transform!!.up) { floatArrayOf(x, y, z) }
            ))
        }

        // Update position of audio sources
        scene.getComponents<AudioSourceComponent>().values.forEach { ac ->
            ac.source.position = ac.transform!!.position
        }
    }

    override fun render(scene: Scene) = Unit
}