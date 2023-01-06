package breakout

import breakout.components.BallComponent
import breakout.components.BoxCollider
import com.curiouscreature.kotlin.math.Float2
import ecs.Entity
import ecs.Scene
import ecs.components.ModelComponent
import resources.Material
import resources.Mesh

class Spawner(val scene: Scene) {

    /** Spawns a ball. */
    fun ball(position: Float2, size: Float2, velocity: Float2) = Entity(
        BallComponent(
            velocity = velocity,
            dimensions = Float2(1f, 1f)
        ),
        ModelComponent(
            mesh = breakoutRegistry.mesh["ball"]!!,
            material = Material(texture = breakoutRegistry.texture["test"]!!),
            shaderProgram = breakoutRegistry.shaderProgram["test"]!!
        )
    ).also { e -> scene.add(e) }.apply {
        transform.position.xy = position
        transform.scale.xy = size
    }

    /** Spawns a brick. */
    fun brick(position: Float2, size: Float2 = Float2(.2f, .1f)) = Entity(
        BoxCollider(
            offset = Float2(0f),
            dimensions = Float2(1f, 1f)
        ),
        ModelComponent(
            mesh = Mesh.QUAD,
            material = Material(texture = breakoutRegistry.texture["test"]!!),
            shaderProgram = breakoutRegistry.shaderProgram["test"]!!
        )
    ).also { e -> scene.add(e) }.apply {
        transform.position.xy = position
        transform.scale.xy = size
    }
}