package breakout

import GameProperties
import breakout.systems.*
import com.curiouscreature.kotlin.math.Float2
import ecs.Camera
import ecs.ECSystem
import ecs.Entity
import ecs.Scene
import ecs.systems.SimpleAudioSystem
import ecs.systems.SimpleCameraFetchingSystem
import ecs.systems.SimpleModelRenderSystem
import io.InputManager

fun newBreakoutScene(properties: GameProperties) = Scene(
    SimpleCameraFetchingSystem(), // Fetch camera
    ControllerSystem(), // Update pad
    BallSystem(), // Update balls
    CollisionSystem(), // Collision handling
    PowerupSystem(),
    HealthSystem(), // Clear dead entities
    SimpleModelRenderSystem(), // Render models
    SimpleAudioSystem(), // Play audio
    object : ECSystem() { // Testing
        override fun update(scene: Scene, inputManager: InputManager, delta: Double) {}

        override fun render(scene: Scene) {}

    }
).apply {

    val spawner = Spawner(this)

    // Add camera
    val aspectRatio = properties.windowDefaultWidth.toFloat() / properties.windowDefaultHeight
    add(Entity(Camera.ortho(-aspectRatio, aspectRatio, -1f, 1f, 1f, -1f)))

    // Spawn bricks
    val bricksCenter = Float2(0f, .5f)
    val numBricksX = 10
    val numBricksY = 5
    val brickSpread = Float2(.25f, .15f)
    val offset = - Float2((numBricksX - 1).toFloat(), (numBricksY - 1).toFloat()) * brickSpread / 2f
    for (i in 0 until numBricksX) {
        for (j in 0 until numBricksY) {
            spawner.brick(
                position = Float2(i* brickSpread.x, j * brickSpread.y) + bricksCenter + offset,
            )
        }
    }

    // Spawn walls
    spawner.wall(Float2(0f, 1f), Float2(4f, .1f)) // Top
    spawner.wall(Float2(-1.5f, 0f), Float2(.1f, 2f)) // Left
    spawner.wall(Float2(1.5f, 0f), Float2(.1f, 2f)) // Right


    // Spawn ball
    spawner.ball(
        position = Float2(0f, .2f),
        size = Float2(.03f),
        velocity = Float2(.3f, .6f)
    )

    // Spawn pad
    spawner.pad(
        position = Float2(.0f, -.9f),
        size = Float2(0.3f, .05f),
        speed = 2f
    )
}