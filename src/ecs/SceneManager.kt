package ecs

import io.InputManager

/** Keeps track of what [Scene] is currently active, and allows it to be updated with the [loop] method. */
class SceneManager {

    /** The current scene, or null if there is none. */
    var currentScene: Scene? = null
        private set(scene) { scene?.manager = this; field = scene }

    private var nextScene: Scene? = null

    /**
     * Calls [update] and [render] on the [currentScene] in sequential order. If a non-immediate [swap] has been issued
     * when the end of this method is reached, the [currentScene] will be swapped for the new candidate. An
     * [inputManager] is also required for enabling user input in the current scene's [update][Scene.update] method.
     * Likewise, a [delta] value should also be supplied, indicating how many seconds have passed since
     * the previous call.
     */
    fun loop(inputManager: InputManager, delta: Double) {
        update(inputManager, delta)
        render()
        nextScene?.let { s -> currentScene = s; nextScene = null }
    }

    /**
     * Calls the [update][Scene.update] method on the [currentScene]. Note that if a non-immediate [swap] has been
     * issued, calling this method will not perform it. Only the [loop] method has this capability. Note that an
     * [inputManager] is also required for forwarding to [Scene.update]. Likewise, a [delta] value should also be
     * supplied, indicating how many seconds have passed since the previous update.
     */
    fun update(inputManager: InputManager, delta: Double) = currentScene?.apply { update(inputManager, delta) }

    /**
     * Calls the [render][Scene.render] method on the [currentScene]. Note that if a non-immediate [swap] has been
     * issued, calling this method will not perform it. Only the [loop] method has this capability.
     */
    fun render() = currentScene?.apply { render() }

    /**
     * Indicates that the [currentScene] should be swapped for the specified [newScene].
     *
     * Note that if [immediate] is set to true the [currentScene] will be swapped immediately. By default, it will
     * instead be swapped at the end of the [loop] method, in order to guarantee a complete game iteration. If multiple
     * swaps are issued before a swap is performed, only the latest one will be used.
     */
    fun swap(newScene: Scene, immediate: Boolean = false) =
        if (immediate) { currentScene = newScene } else { nextScene = newScene }
}