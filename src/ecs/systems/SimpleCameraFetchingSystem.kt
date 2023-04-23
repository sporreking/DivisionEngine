package ecs.systems

import ecs.Camera
import ecs.ECSystem
import ecs.Scene
import io.InputManager

/** Fetches the first available camera component and attaches it to the current scene. */
class SimpleCameraFetchingSystem : ECSystem() {
    override fun update(scene: Scene, inputManager: InputManager, delta: Double) {
        if (scene.camera == null) scene.camera = scene.getComponents<Camera>().values.firstOrNull()
    }

    override fun render(scene: Scene) = Unit
}