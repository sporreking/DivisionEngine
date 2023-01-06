package breakout

import GameProperties
import breakout.components.BallComponent
import breakout.components.BoxCollider
import breakout.systems.BallSystem
import com.curiouscreature.kotlin.math.Float2
import ecs.Camera
import ecs.ECSystem
import ecs.Entity
import ecs.Scene
import ecs.components.ModelComponent
import ecs.systems.SimpleAudioSystem
import ecs.systems.SimpleCameraFetchingSystem
import ecs.systems.SimpleModelRenderSystem
import io.InputManager
import resources.Material
import resources.Mesh

fun newBreakoutScene(properties: GameProperties) = Scene(
    SimpleCameraFetchingSystem(), // Fetch camera
    BallSystem(), // Update balls
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

    // Spawn ball
    spawner.ball(
        position = Float2(0f, .2f),
        size = Float2(.01f),
        velocity = Float2(.2f, .3f)
    )
}