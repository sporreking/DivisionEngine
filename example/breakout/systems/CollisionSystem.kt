package breakout.systems

import breakout.components.*
import ecs.ECSystem
import ecs.Scene
import io.InputManager
import io.Logger

class CollisionSystem : ECSystem() {

    override fun update(scene: Scene, inputManager: InputManager, delta: Double) {

        // Update bricks
        scene.getComponents<CollisionReductor>().values.forEach { collisionReductor ->
            val collider = collisionReductor.parent!!.get<BoxCollider>() ?: return@forEach
            val health = collisionReductor.parent!!.get<HealthComponent>() ?: return@forEach

            // Simple collision handling
            health.health -= collider.collisions.size * collisionReductor.damagePerCollision
            collider.collisions.clear()
        }

        // Update pad
        scene.getComponents<BounceAccelerator>().values.forEach { bounceAccelerator ->
            val collider = bounceAccelerator.parent!!.get<BoxCollider>() ?: return@forEach

            collider.collisions.forEach { info ->
                scene.getEntity(info.id)?.get<BallComponent>()?.also { ball ->
                    ((info.location - collider.transform!!.position.xy).x / collider.transform!!.scale.x).also { a ->
                        ball.velocity.x += a * bounceAccelerator.maxForce
                        Logger.debug(a * bounceAccelerator.maxForce)
                    }
                }
            }

            collider.collisions.clear()
        }
    }

    override fun render(scene: Scene) = Unit
}