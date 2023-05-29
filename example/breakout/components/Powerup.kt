package breakout.components

import ecs.Component
import io.SceneSaver
import io.StorageHandler

enum class PowerupEffect {
    SPEED_UP,
    SIZE_UP;

    init {
        SceneSaver.registerStorageHandler(StorageHandler(
            { data, _ -> data.name },
            { text, _ -> PowerupEffect.valueOf(text) }
        ))
    }
}

data class Powerup(
    val effect: PowerupEffect,
    val fallSpeed: Float = 1f
) : Component()