package breakout

import DivisionEngine
import GameProperties
import com.curiouscreature.kotlin.math.Float4
import io.Axis
import io.InputManager
import org.lwjgl.glfw.GLFW

const val MOVE_SPEED = 1.0f

const val AXIS_MOVE_X: Axis = 1
const val AXIS_MOVE_Y: Axis = 2


class Breakout {
    companion object {

        private fun registerInput(inputManager: InputManager) {

            // Movement setup
            inputManager.initAxis(AXIS_MOVE_X, MOVE_SPEED)
            inputManager.initAxis(AXIS_MOVE_Y, MOVE_SPEED)

            inputManager.bindGamepadAxisToAxis(
                AXIS_MOVE_X,
                0,
                GLFW.GLFW_GAMEPAD_AXIS_LEFT_X,
                .5f,
                false
            )

            inputManager.bindGamepadAxisToAxis(
                AXIS_MOVE_Y,
                0,
                GLFW.GLFW_GAMEPAD_AXIS_LEFT_Y,
                .5f,
                true
            )
        }

        fun run() {
            val properties = GameProperties(
                name = "Breakout",
                windowDefaultWidth = 1280,
                windowDefaultHeight = 720,
                windowClearColor = Float4(0f, 0f, 0f, 1f)
            )

            DivisionEngine(properties).also {
                    engine -> registerInput(engine.inputManager)
            }.run(newBreakoutScene(properties))
        }
    }
}