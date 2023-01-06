package breakout.components

import com.curiouscreature.kotlin.math.Float2
import ecs.Component

data class BoxCollider(
    var offset: Float2,
    var dimensions: Float2,
    var collisions: MutableList<Long> = mutableListOf()
) : Component()