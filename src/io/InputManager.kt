package io

import com.curiouscreature.kotlin.math.Float2
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.glfw.GLFWGamepadState
import kotlin.math.abs
import kotlin.math.max

/** Unique identifier of a button. */
typealias Button = Int

/** Represents the state of a button. */
enum class ButtonState {

    /** The button is not being pressed down. */
    UP,

    /** The button is being pressed down. */
    DOWN,

    /** The button was pressed this frame. The frame also counts as [DOWN]. */
    PRESSED,

    /** The button was released this frame. The frame also counts as [UP]. */
    RELEASED
}

/** Unique identifier of an axis. */
typealias Axis = Int

/** The configuration of an axis. */
data class AxisConfig(
    /** The axis to which the configuration applies. */
    val axis: Axis,

    /** The sensitivity of the axis. */
    var sensitivity: Float,

    /**
     * Whether the axis should be inverted. Note that axes with button input are
     * positive if [invert] is set to false (default).
     */
    var invert: Boolean = false
)

/** Represents the state of an axis. */
data class AxisState(
    /** The magnitude limit of the axis. */
    val limit: Float,

    /**
     * The static value of the axis. This value is increased or decreased at arbitrary points of time due to events
     * such as button presses or releases. To get the actual value of the axis, see [value].
     */
    var staticValue: Float,

    /**
     * The dynamic value of the axis. This value is derived each frame according to current axis input, and is reset
     * at the end of each frame. To get the actual value of the axis, see [value].
     */
    var dynamicValue: Float
) {
    /** The actual value of the axis. This value may exceed the [limit], and must be clamped manually before use. */
    val value get() = staticValue + dynamicValue
}

/** The configuration of a game pad. */
data class GamepadConfig(
    /** The ID of this gamepad. This corresponds to one of the GLFW joystick constants, e.g., `GLFW_JOYSTICK_1`.  */
    val id: Int
) {
    /** Maps physical gamepad buttons to virtual buttons. */
    val buttonMap: MutableMap<Int, Button> = mutableMapOf()

    /** Maps physical gamepad buttons to virtual axes. */
    val buttonAxisMap: MutableMap<Int, AxisConfig> = mutableMapOf()

    /** Maps physical gamepad axes to virtual axes. */
    val axisMap: MutableMap<Int, AxisConfig> = mutableMapOf()

    /**
     * Used to keep track of the minimum physical axis input force that must be exerted.
     * Values that fall below these thresholds will be considered as zeroes.
     */
    val axisThresholds: MutableMap<Int, Float> = mutableMapOf()
    val heldButtons: MutableSet<Button> = mutableSetOf()
}

/** Manages controller, keyboard, and mouse input etc. */
class InputManager(
    /** The window to which the input manager applies. */
    private val window: Long
) {

    /** The keyboard callback method to use for this input manager. */
    fun keyCallback(window: Long, key: Int, scancode: Int, action: Int, mods: Int) {
        if (action == GLFW_REPEAT) return

        // Register key presses and releases
        keyButtonMap[key]?.let { b -> regButtonInput(b, action == GLFW_PRESS) }

        // Update bound axes
        keyAxisMap[key]?.let { a ->
            axisStates[a.axis]!!.staticValue += if (!a.invert == (action == GLFW_PRESS)) a.sensitivity else -a.sensitivity
        }
    }

    /** The mouse button callback method to use for this input manager. */
    fun mouseButtonCallback(window: Long, button: Int, action: Int, mods: Int) {

        // Register key presses and releases
        mouseButtonMap[button]?.let { b -> regButtonInput(b, action == GLFW_PRESS) }

        // Update bound axes
        mouseAxisMap[button]?.let { a ->
            axisStates[a.axis]!!.staticValue += if (!a.invert == (action == GLFW_PRESS)) a.sensitivity else -a.sensitivity
        }
    }

    /** The mouse position callback method to use for this input manager. */
    fun mousePositionCallback(window: Long, xPos: Double, yPos: Double) {
        mouseDelta.x += xPos.toFloat() - mousePosition.x
        mouseDelta.y += yPos.toFloat() - mousePosition.y
        mousePosition.x = xPos.toFloat()
        mousePosition.y = yPos.toFloat()
    }

    /** Register button presses and releases. */
    private fun regButtonInput(button: Int, pressed: Boolean) {
        buttonStates[button] = when (buttonStates[button]) {
            ButtonState.UP, ButtonState.PRESSED ->
                if (pressed) ButtonState.PRESSED else ButtonState.UP
            ButtonState.DOWN, ButtonState.RELEASED ->
                if (pressed) ButtonState.DOWN else ButtonState.RELEASED
            else -> if (pressed) ButtonState.PRESSED else ButtonState.RELEASED
        }
    }

    /** Should be called at the end of every frame before event processing. */
    fun update() {
        // Update button states
        buttonStates.forEach { (b, s) -> when (s) {
            ButtonState.PRESSED -> buttonStates[b] = ButtonState.DOWN
            ButtonState.RELEASED -> buttonStates[b] = ButtonState.UP
            else -> Unit
        }}

        // Update gamepad states
        gamepadIDs.forEach { id ->
            gamepadMap.filter { (_, config) -> config.id == id }.forEach { (gamepad, config) ->
                // Reset axis input
                config.axisMap.values.forEach { ac ->
                    axisStates[ac.axis]!!.dynamicValue = 0f
                }

                // Get gamepad states
                val state = GLFWGamepadState.create()
                glfwGetGamepadState(config.id, state)

                // Container for button events
                val gamepadButtonEvents = mutableMapOf<Int, Boolean>()

                // Find button events
                fun inferGamepadButtonEvent(gamepadButton: Int) {
                    // Check whether button is being held down
                    val down = state.buttons(gamepadButton).toInt() == GLFW_PRESS

                    if (down && !config.heldButtons.contains(gamepadButton)) {
                        // Button was pressed this frame
                        gamepadButtonEvents[gamepadButton] = true
                    } else if (!down && config.heldButtons.contains(gamepadButton)) {
                        // Button was released this frame
                        gamepadButtonEvents[gamepadButton] = false
                    }
                }

                // Update button map
                config.buttonMap.forEach { (gamepadButton, button) ->
                    inferGamepadButtonEvent(gamepadButton)
                    gamepadButtonEvents[gamepadButton]?.let { pressed -> regButtonInput(button, pressed) }
                }

                // Update button axes
                config.buttonAxisMap.forEach { (gamepadButton, ac) ->
                    inferGamepadButtonEvent(gamepadButton)
                    gamepadButtonEvents[gamepadButton]?.let { pressed ->
                        axisStates[ac.axis]!!.staticValue += if (!ac.invert == pressed) ac.sensitivity else -ac.sensitivity
                    }
                }

                // Update regular axes
                config.axisMap.forEach { (gamepadAxis, ac) ->
                    val v = state.axes(gamepadAxis).let { v ->
                        if (abs(v) < config.axisThresholds[gamepadAxis]!!) 0f else v
                    }
                    axisStates[ac.axis]!!.dynamicValue += v * ac.sensitivity * if (ac.invert) -1 else 1
                    //if (state.axes(gamepadAxis) > .1f) println("${config.axisThresholds[gamepadAxis]}, ${state.axes(gamepadAxis)}")
                }

                // Update conception of what buttons are being held down
                gamepadButtonEvents.forEach { (button, pressed) ->
                    if (pressed) config.heldButtons.add(button) else config.heldButtons.remove(button)
                }
            }
        }

        // Reset mouse delta
        mouseDelta = Float2(0f, 0f)
    }

    /** Returns true if the specified [button] is up, i.e., not being pressed down. */
    fun up(button: Button) = buttonStates[button] == ButtonState.UP || released(button) || buttonStates[button] == null

    /** Returns true if the specified [button] is currently being pressed down. */
    fun down(button: Button) = buttonStates[button] == ButtonState.DOWN || pressed(button)

    /** Returns true if the specified [button] was pressed this frame. */
    fun pressed(button: Button) = buttonStates[button] == ButtonState.PRESSED

    /** Returns true if the specified [button] was released this frame. */
    fun released(button: Button) = buttonStates[button] == ButtonState.RELEASED

    /**
     * Returns the value of the specified [axis], or zero if the axis does not exist.
     *
     * Non-mouse input sources of the axis will be scaled by the specified [scalar]. Note that this is particularly
     * useful for adjusting keyboard and controller axis input to a time delta, without affecting mouse input.
     */
    fun axis(axis: Axis, scalar: Double = 1.0) = axisStates[axis]?.run {
        if (limit >= 0) axisValue(axis, scalar).coerceIn(-limit, limit) else axisValue(axis, scalar)
    } ?: 0f

    /** Binds a keyboard [key] to a [button]. */
    fun bindKeyToButton(button: Button, key: Int) { keyButtonMap[key] = button }

    /** Binds a [mouseButton] to a [button]. */
    fun bindMouseButtonToButton(button: Button, mouseButton: Int) { mouseButtonMap[mouseButton] = button }

    /**
     * Binds the [gamepadButton] of the specified [gamepad] to a [button]. The [gamepad] should simply be an integer of
     * 0, 1, 2, etc. depending on what gamepad should be used.
     */
    fun bindGamepadButtonToButton(button: Button, gamepad: Int, gamepadButton: Int) {
        gamepadConfig(gamepad).buttonMap[gamepadButton] = button
    }

    /**
     * Initializes an [axis] such that input may be bound to it. A [limit] may be specified, clamping the magnitude of
     * the axis value. If no clamping is desired, [limit] should be set to a negative value (default is -1).
     */
    fun initAxis(axis: Axis, limit: Float = -1f) { axisStates[axis] = AxisState(limit, 0f, 0f) }

    /**
     * Binds two keys to an [axis]. Whenever the [positiveKey] is being held down, it will count as if the axis is
     * being pulled in a positive direction by the specified [sensitivity]. Likewise, holding down the [negativeKey]
     * counts as pulling the axis in a negative direction by the same amount. Note that the axis must have been
     * initialized prior to calling this method, using [initAxis].
     */
    fun bindKeysToAxis(axis: Axis, positiveKey: Int, negativeKey: Int, sensitivity: Float = 1f) {
        checkAxisInitialized(axis)
        checkSensitivity(sensitivity)
        keyAxisMap[positiveKey] = AxisConfig(axis, sensitivity, false)
        keyAxisMap[negativeKey] = AxisConfig(axis, sensitivity, true)
    }

    /**
     * Binds two mouse buttons to an [axis]. Whenever the [positiveMouseButton] is being held down, it will count as if
     * the axis is being pulled in a positive direction by the specified [sensitivity]. Likewise, holding down the
     * [negativeMouseButton] counts as pulling the axis in a negative direction by the same amount. Note that the axis
     * must have been initialized prior to calling this method, using [initAxis].
     */
    fun bindMouseButtonsToAxis(axis: Axis, positiveMouseButton: Int,
                               negativeMouseButton: Int, sensitivity: Float = 1f) {
        checkAxisInitialized(axis)
        checkSensitivity(sensitivity)
        mouseAxisMap[positiveMouseButton] = AxisConfig(axis, sensitivity, false)
        mouseAxisMap[negativeMouseButton] = AxisConfig(axis, sensitivity, true)
    }

    /**
     * Binds mouse X movement to the specified [axis]. The native axis value is scaled by the [sensitivity] factor,
     * and is negated if [invert] is set to true. Note that the axis must have been initialized prior to calling
     * this method, using [initAxis].
     */
    fun bindMouseXToAxis(axis: Axis, sensitivity: Float, invert: Boolean = false) {
        checkAxisInitialized(axis)
        checkSensitivity(sensitivity)
        mouseMoveXConfig = AxisConfig(axis, sensitivity, invert)
    }

    /**
     * Binds mouse X movement to the specified [axis]. The native axis value is scaled by the [sensitivity] factor,
     * and is negated if [invert] is set to true. Note that the axis must have been initialized prior to calling
     * this method, using [initAxis].
     */
    fun bindMouseYToAxis(axis: Axis, sensitivity: Float, invert: Boolean = false) {
        checkAxisInitialized(axis)
        checkSensitivity(sensitivity)
        mouseMoveYConfig = AxisConfig(axis, sensitivity, invert)
    }

    /**
     * Binds two gamepad buttons to an [axis]. Whenever the [positiveGamepadButton] is being held down on the specified
     * [gamepad], it will count as if the axis is being pulled in a positive direction by the specified [sensitivity].
     * Likewise, holding down the [negativeGamepadButton] counts as pulling the axis in a negative direction by the same
     * amount. Note that the axis must have been initialized prior to calling this method, using [initAxis]. The
     * [gamepad] should simply be an integer of 0, 1, 2, etc. depending on what gamepad should be used.
     */
    fun bindGamepadButtonsToAxis(axis: Axis, gamepad: Int, positiveGamepadButton: Int,
                                 negativeGamepadButton: Int, sensitivity: Float) {
        checkAxisInitialized(axis)
        checkSensitivity(sensitivity)
        with(gamepadConfig(gamepad)) {
            buttonAxisMap[positiveGamepadButton] = AxisConfig(axis, sensitivity, false)
            buttonAxisMap[negativeGamepadButton] = AxisConfig(axis, sensitivity, true)
        }
    }

    /**
     * Binds the [gamepadAxis] of the specified [gamepad] to an [axis]. The physical axis input will be scaled by
     * the [sensitivity], and negated if [invert] is set to true. The [gamepad] should simply be an integer of
     * 0, 1, 2, etc. depending on what gamepad should be used.
     *
     * To counter input noise, physical input smaller than [threshold] will be considered as zero. Note that the
     * threshold is oblivious to the [sensitivity]; the [threshold] should lie between 0 and 1 since this is the
     * raw axis input range.
     */
    fun bindGamepadAxisToAxis(axis: Axis, gamepad: Int, gamepadAxis: Int,
                              sensitivity: Float, invert: Boolean, threshold: Float = .1f) {
        checkAxisInitialized(axis)
        checkSensitivity(sensitivity)
        check(threshold in 0.0..1.0) { "The threshold is applied to raw input must thus lie within range [0,1]." }
        with(gamepadConfig(gamepad)) {
            axisMap[gamepadAxis] = AxisConfig(axis, sensitivity, invert)
            axisThresholds[gamepadAxis] = threshold
        }
    }

    /** Controls whether the mouse should be grabbed or not. */
    var mouseGrabbed: Boolean get() = glfwGetInputMode(window, GLFW_CURSOR) == GLFW_CURSOR_DISABLED
        set(grab) { glfwSetInputMode(window, GLFW_CURSOR, if (grab) GLFW_CURSOR_DISABLED else GLFW_CURSOR_NORMAL) }

    /** The current position of the mouse. */
    var mousePosition = Float2(0f, 0f)
        private set

    /** The mouse position difference between the last two mouse position callbacks. */
    private var mouseDelta = Float2(0f, 0f)

    /** Throws an [IllegalStateException] if the specified sensitivity is less than zero. */
    private fun checkSensitivity(sensitivity: Float) = check(sensitivity >= 0f) {
        "Sensitivity must not be a negative value!"
    }

    /** Throws an [IllegalStateException] if the specified axis has not been initialized. */
    private fun checkAxisInitialized(axis: Axis) = checkNotNull(axisStates[axis]) {
        "Axis $axis must be initialized before binding!"
    }

    private fun axisValue(axis: Axis, scalar: Double) = axisStates[axis]?.run {

        fun mouseAxisConfigValue(config: AxisConfig, delta: Float) =
            if (config.axis == axis) delta * config.sensitivity * if (config.invert) -1f else 1f else 0f

        (value * scalar +
                (mouseMoveXConfig?.let { mouseAxisConfigValue(it, mouseDelta.x) } ?: 0f) +
                (mouseMoveYConfig?.let { mouseAxisConfigValue(it, mouseDelta.y) } ?: 0f))
            .toFloat()
    } ?: 0f

    private fun gamepadConfig(gamepad: Int): GamepadConfig {
        return (gamepadMap[gamepad] ?: GamepadConfig(gamepad)).also { m -> gamepadMap[gamepad] = m }
    }

    private val gamepadIDs get() = buildList {
        (GLFW_JOYSTICK_1..GLFW_JOYSTICK_LAST).forEach { id ->
            if (glfwJoystickIsGamepad(id)) add(id)
        }
    }

    // Button mappings
    private val keyButtonMap: MutableMap<Int, Button> = mutableMapOf()
    private val mouseButtonMap: MutableMap<Int, Button> = mutableMapOf()
    private val buttonStates: MutableMap<Button, ButtonState> = mutableMapOf()

    // Axis mappings
    private val keyAxisMap: MutableMap<Int, AxisConfig> = mutableMapOf()
    private val mouseAxisMap: MutableMap<Int, AxisConfig> = mutableMapOf()
    private var mouseMoveXConfig: AxisConfig? = null
    private var mouseMoveYConfig: AxisConfig? = null
    private val axisStates: MutableMap<Axis, AxisState> = mutableMapOf()

    // Gamepad map
    private val gamepadMap: MutableMap<Int, GamepadConfig> = mutableMapOf()
}