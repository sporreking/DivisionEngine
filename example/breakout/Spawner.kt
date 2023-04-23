package breakout

import breakout.components.*
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
        ),
        CollisionReductor(),
        HealthComponent(1f),
        PowerupSpawner()
    ).also { e -> scene.add(e) }.apply {
        transform.position.xy = position
        transform.scale.xy = size
    }

    fun pad(position: Float2, size: Float2 = Float2(.15f, .05f), speed: Float = 1f) = Entity(
        BoxCollider(offset = Float2(0f), dimensions = Float2(1f, 1f)),
        ModelComponent(
            mesh = Mesh.QUAD,
            material = Material(texture = breakoutRegistry.texture["test"]!!),
            shaderProgram = breakoutRegistry.shaderProgram["test"]!!
        ),
        Controller(speed = speed),
        BounceAccelerator()
    ).also { e -> scene.add(e) }.apply {
        transform.position.xy = position
        transform.scale.xy = size
    }

    fun wall(position: Float2, size: Float2, visible: Boolean = false) = Entity(
        BoxCollider(offset = Float2(0f), dimensions = Float2(1f, 1f)),
        ModelComponent(
            mesh = Mesh.QUAD,
            material = Material(texture = breakoutRegistry.texture["test"]!!),
            shaderProgram = breakoutRegistry.shaderProgram["test"]!!
        )
    ).also { e -> scene.add(e) }.apply {
        transform.position.xy = position
        transform.scale.xy = size
    }

    fun powerup(position: Float2, size: Float2, effect: PowerupEffect, fallSpeed: Float = .5f) = Entity(
        Powerup(effect = effect, fallSpeed = fallSpeed),
        ModelComponent(
            mesh = Mesh.QUAD,
            material = Material(texture = breakoutRegistry.texture["pu_size_up"]!!),
            shaderProgram = breakoutRegistry.shaderProgram["test"]!!
        )
    ).also { e -> scene.add(e) }.apply {
        transform.position.xy = position
        transform.scale.xy = size
    }
}