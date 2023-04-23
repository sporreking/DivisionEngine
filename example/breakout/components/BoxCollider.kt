package breakout.components

import com.curiouscreature.kotlin.math.Float2
import ecs.Component

data class CollisionInfo(val id: Long, val location: Float2)

data class BoxCollider(
    var offset: Float2,
    var dimensions: Float2,
    var collisions: MutableList<CollisionInfo> = mutableListOf()
) : Component()