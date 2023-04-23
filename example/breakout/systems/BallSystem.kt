package breakout.systems

import breakout.AABB
import breakout.AXIS_MOVE_X
import breakout.AXIS_MOVE_Y
import breakout.components.BallComponent
import breakout.components.BoxCollider
import breakout.components.CollisionInfo
import com.curiouscreature.kotlin.math.Float2
import ecs.ECSystem
import ecs.Scene
import io.InputManager
import io.Logger

class BallSystem : ECSystem() {

    override fun update(scene: Scene, inputManager: InputManager, delta: Double) {
        scene.getComponents<BallComponent>().values.forEach { ballComponent ->
            // Create AABBs for collision check
            val prevBallAABB = AABB(
                ballComponent.transform!!.position.xy,
                ballComponent.dimensions * ballComponent.transform!!.scale.xy
            )

            // Update position based on current velocity
            ballComponent.transform!!.position.xy += ballComponent.velocity * delta.toFloat()

            val ballAABB = AABB(
                ballComponent.transform!!.position.xy,
                ballComponent.dimensions * ballComponent.transform!!.scale.xy
            )

            // Mark colliders with collision
            scene.getComponents<BoxCollider>().values.forEach { boxCollider ->
                collision(prevBallAABB, ballAABB, ballComponent, boxCollider)
            }
        }
    }

    private fun collision(prevBallAABB: AABB, ballAABB: AABB, ball: BallComponent, collider: BoxCollider) {
        // Create collider AABB
        val colliderAABB = AABB(
            collider.transform!!.position.xy,
            collider.dimensions * collider.transform!!.scale.xy
        )

        // Check for collision
        val info = ballAABB.intersection(colliderAABB)

        // Do nothing if there was no collision
        if (!info.intersecting) return

        // Add collision
        collider.collisions.add(CollisionInfo(ball.parent!!.id, ballAABB.position))

        // Update velocity based on placement in previous frame TODO: FIX EDGE CASES
        if (prevBallAABB leftOf colliderAABB) {
            if (prevBallAABB above colliderAABB) Logger.debug("TOP LEFT") // Collision from top left
            else if (prevBallAABB below colliderAABB) Logger.debug("BOTTOM LEFT") // Collision from bottom left
            else ball.velocity.x = -ball.velocity.x // Collision from left

        } else if (prevBallAABB rightOf colliderAABB) {
            if (prevBallAABB above colliderAABB) Logger.debug("TOP RIGHT") // Collision from top right
            else if (prevBallAABB below colliderAABB) Logger.debug("BOTTOM RIGHT") // Collision from bottom right
            else ball.velocity.x = -ball.velocity.x // Collision from right

        } else if (prevBallAABB above colliderAABB) {
            // Collision from above
            ball.velocity.y = -ball.velocity.y

        } else if (prevBallAABB below colliderAABB) {
            // Collision from below
            ball.velocity.y = -ball.velocity.y
        }
    }

    override fun render(scene: Scene) = Unit
}