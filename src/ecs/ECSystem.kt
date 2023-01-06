package ecs

import io.InputManager

abstract class ECSystem {
    /**
     * Updates the specified [scene] using this system. The [inputManager] may be used to handle user input.
     * A time [delta] is also supplied, indicating how much time has passed since the previous update call (in seconds).
     */
    abstract fun update(scene: Scene, inputManager: InputManager, delta: Double)

    /** Renders the specified [scene] using this system. */
    abstract fun render(scene: Scene)
}