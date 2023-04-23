import breakout.Breakout
import io.Axis
import io.Button
import io.LogLevel
import io.Logger

// DONE: SM, Model(mesh, material, shader), Camera, Input, Time
// ON HOLD: Spawner, Save state (use SerializersModule / SerializationStrategy(for parent entity etc))

// DONE: Audio, text, controller input, add Kdoc for classes

// TODO: autoformatting (constructor spacing etc.)
// TODO: hooks, float conversion (1.0f -> 1f)
// TODO: Move load logic for resources, separate camera updater
// TODO: Make game (breakout)

// TODO: text max width/height, default meshes/materials etc, Custom # of textures, animations, 2D-physics, UI
// TODO: hierarchical entities, 2D-lighting, Custom render target for scenes etc.
// TODO: Make game (2D platformer)

fun main() {
    // Set logger level
    Logger.level = LogLevel.DEBUG

    Breakout.run()
}