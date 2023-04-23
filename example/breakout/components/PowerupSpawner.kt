package breakout.components

import ecs.Component

data class PowerupSpawner(
    val probability: Float = 1f
) : Component()