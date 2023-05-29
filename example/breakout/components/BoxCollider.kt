package breakout.components

import com.curiouscreature.kotlin.math.Float2
import ecs.Component
import io.SceneSaver
import io.StorageHandler

data class CollisionInfo(val id: Long, val location: Float2) {
    init {
        SceneSaver.registerStorageHandler(StorageHandler(
            { data, _ -> "${data.id};${data.location.x};${data.location.y}" },
            { text, _ -> text.split(';').let { t ->
                CollisionInfo(t[0].toLong(), Float2(t[1].toFloat(), t[2].toFloat()))
            }}
        ))
    }
}

data class BoxCollider(
    var offset: Float2,
    var dimensions: Float2,
    var collisions: MutableList<CollisionInfo> = mutableListOf()
) : Component()