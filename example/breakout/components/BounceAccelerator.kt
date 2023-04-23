package breakout.components

import ecs.Component

data class BounceAccelerator(
    var maxForce: Float = 1.0f
) : Component()