package breakout.components

import ecs.Component

data class CollisionReductor(
    var damagePerCollision: Float = 1f
) : Component()