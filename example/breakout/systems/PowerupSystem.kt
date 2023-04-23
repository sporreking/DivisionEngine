package breakout.systems

import breakout.AABB
import breakout.Spawner
import breakout.components.*
import com.curiouscreature.kotlin.math.Float2
import ecs.ECSystem
import ecs.Scene
import io.InputManager
import kotlin.random.Random

class PowerupSystem : ECSystem() {

    val random = Random(0)

    fun activatePowerup(scene: Scene, effect: PowerupEffect) {
        if (effect == PowerupEffect.SIZE_UP) {
            scene.getComponents<BallComponent>().values.forEach { ball ->
                ball.transform!!.scale.xy += Float2(.02f, .02f)
            }
        } else if (effect == PowerupEffect.SPEED_UP) {
            scene.getComponents<BallComponent>().values.forEach { ball ->
                ball.velocity.y += .02f * (if (ball.velocity.y < 0) -1 else 1)
            }
        }
    }

    override fun update(scene: Scene, inputManager: InputManager, delta: Double) {

        val spawner = Spawner(scene)

        val bin = mutableSetOf<Long>()

        // Spawn powerups on spawner death
        scene.getComponents<PowerupSpawner>().values.forEach { puSpawner ->
            if (puSpawner.parent!!.get<HealthComponent>()?.dead ?: return@forEach) {
                if (random.nextFloat() < puSpawner.probability) {
                    spawner.powerup(
                        puSpawner.transform!!.position.xy,
                        Float2(.05f, .05f),
                        PowerupEffect.values()[random.nextInt(PowerupEffect.values().size)],
                    )
                }
            }
        }

        // Gravity
        scene.getComponents<Powerup>().values.forEach { pu ->
            pu.transform!!.position.y -= pu.fallSpeed * delta.toFloat()
        }

        // Pickup
        scene.getComponents<BounceAccelerator>().values.forEach { ba ->
            scene.getComponents<Powerup>().values.forEach { pu ->
                if (AABB(pu.transform!!.position.xy, pu.transform!!.scale.xy).intersection(
                    AABB(ba.transform!!.position.xy, ba.transform!!.scale.xy)
                ).intersecting) {
                    // Pickup!
                    bin.add(pu.parent!!.id)
                    activatePowerup(scene, pu.effect)
                }
            }
        }

        // Remove powerups
        bin.forEach(scene::remove)
    }

    override fun render(scene: Scene) = Unit
}