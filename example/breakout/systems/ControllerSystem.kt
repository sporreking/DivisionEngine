package breakout.systems

import breakout.AXIS_MOVE_X
import breakout.components.Controller
import ecs.ECSystem
import ecs.Scene
import io.InputManager

class ControllerSystem : ECSystem() {
    override fun update(scene: Scene, inputManager: InputManager, delta: Double) {
        scene.getComponents<Controller>().values.forEach { controller ->
            controller.transform!!.position.x += inputManager.axis(AXIS_MOVE_X, delta) * controller.speed
        }
    }

    override fun render(scene: Scene) = Unit
}