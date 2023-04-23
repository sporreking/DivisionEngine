package breakout.components

import ecs.Component

data class HealthComponent(
    var health: Float
) : Component() {
    val dead get() = health <= 0
    val alive get() = !dead
}