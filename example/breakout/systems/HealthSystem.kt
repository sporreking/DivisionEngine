package breakout.systems

import breakout.components.HealthComponent
import ecs.ECSystem
import ecs.Scene
import io.InputManager

class HealthSystem : ECSystem() {
    override fun update(scene: Scene, inputManager: InputManager, delta: Double) {

        val bin = mutableSetOf<Long>()

        scene.getComponents<HealthComponent>().values.forEach { health ->
            if (health.dead) bin.add(health.parent!!.id)
        }

        bin.forEach(scene::remove)
    }

    override fun render(scene: Scene) = Unit
}