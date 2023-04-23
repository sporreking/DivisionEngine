package breakout.components

import ecs.Component

enum class PowerupEffect {
    SPEED_UP,
    SIZE_UP
}

data class Powerup(
    val effect: PowerupEffect,
    val fallSpeed: Float = 1.0f
) : Component()