package breakout.components

import com.curiouscreature.kotlin.math.Float2
import ecs.Component

data class BallComponent(
    var velocity: Float2,
    var dimensions: Float2 = Float2(1f, 1f)
) : Component()