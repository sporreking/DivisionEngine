import com.curiouscreature.kotlin.math.Float2
import com.curiouscreature.kotlin.math.Float4
import ecs.Scene
import ecs.SceneManager
import io.InputManager
import io.Logger
import org.lwjgl.Version
import org.lwjgl.glfw.Callbacks.glfwFreeCallbacks
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.glfw.GLFWErrorCallback
import org.lwjgl.openal.AL
import org.lwjgl.openal.ALC
import org.lwjgl.openal.ALC10.*
import org.lwjgl.openal.ALCCapabilities
import org.lwjgl.openal.ALCapabilities
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL20.glUniform1i
import org.lwjgl.system.MemoryStack.stackPush
import org.lwjgl.system.MemoryUtil.NULL
import resources.*
import util.TimeManager
import java.nio.ByteBuffer
import java.nio.IntBuffer

data class GameProperties(
    /** The name of the game. */
    val name: String,
    /** The default width of the game's window (in pixels). */
    val windowDefaultWidth: Int,
    /** The default height of the game's window (in pixels). */
    val windowDefaultHeight: Int,
    /** The color to use for clearing the screen (RGBA). */
    val windowClearColor: Float4
)

/**
 * The main wrapper of the entire division engine application. When the [run] method is called with a starting [Scene],
 * a new window with the specified [properties] is opened, and the scene starts to loop using the [sceneManager].
 */
class DivisionEngine(
    /** The properties of the game. */
    val properties: GameProperties
) {

    private var window = 0L

    /**
     * The core scene manager of this game. The main loop of the game is driven by
     * the [loop][SceneManager.loop] method of this instance.
     */
    val sceneManager = SceneManager()

    // Initialize the game
    init { init() }

    /** The core input manager of this game. Will be forwarded to the [sceneManager] each frame. */
    lateinit var inputManager: InputManager
        private set

    /** Starts the application with the specified [startScene]. */
    fun run(startScene: Scene) {

        // Set start scene
        sceneManager.swap(startScene, immediate = true)

        // Start main loop
        loop()

        // Free window callbacks and destroy window
        glfwFreeCallbacks(window)
        glfwDestroyWindow(window)

        // Terminate GLFW and free error callbacks
        glfwTerminate()
        glfwSetErrorCallback(null)?.free()
    }

    private fun init() {

        // Display LWJGL version
        Logger.info("LWJGL ${Version.getVersion()}")

        // Set error callback
        GLFWErrorCallback.createPrint(System.err).set()

        // Init GLFW
        check(glfwInit()) { "Unable to initialize GLFW!" }

        // Configure GLFW
        glfwDefaultWindowHints()
        glfwWindowHint(GLFW_VISIBLE, GLFW_TRUE)
        glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE)

        // Create window
        window = glfwCreateWindow(properties.windowDefaultWidth, properties.windowDefaultHeight,
            properties.name, NULL, NULL)
        check(window != NULL) { "Failed to create the GLFW window!" }

        // Create input manager
        inputManager = InputManager(window)

        // Setup key callback
        glfwSetKeyCallback(window, inputManager::keyCallback)

        // Setup mouse callbacks
        glfwSetMouseButtonCallback(window, inputManager::mouseButtonCallback)
        glfwSetCursorPosCallback(window, inputManager::mousePositionCallback)

        // Setup joystick callbacks
        glfwSetJoystickCallback { jid, event -> println("$jid, $event") }

        // Create new frame
        stackPush().also { stack ->
            val pWidth = stack.mallocInt(1)
            val pHeight = stack.mallocInt(1)

            glfwGetWindowSize(window, pWidth, pHeight)

            glfwGetVideoMode(glfwGetPrimaryMonitor())?.apply {
                glfwSetWindowPos(
                    window,
                    (width() - pWidth.get(0)) / 2,
                    (height() - pHeight.get(0)) / 2
                )
            }
        }.pop()

        glfwMakeContextCurrent(window)
        glfwSwapInterval(0)
        glfwShowWindow(window)

        // Init GL
        GL.createCapabilities()

        // Init AL
        val device = alcOpenDevice(null as ByteBuffer?)
        check(device != NULL) { "Failed to open default OpenAL device!" }
        val deviceCapabilities = ALC.createCapabilities(device)
        val context = alcCreateContext(device, null as IntBuffer?)
        check(context != NULL) { "Failed to create OpenAL context!" }
        alcMakeContextCurrent(context)
        AL.createCapabilities(deviceCapabilities)
    }

    private fun loop() {
        // TODO: Maybe don't remove?
        glEnable(GL_TEXTURE_2D)
        glEnable(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        with (properties.windowClearColor) { glClearColor(r, g, b, a) }

        // Setup time
        val timeManager = TimeManager()

        // Loop
        while (!glfwWindowShouldClose(window)) {
            glClear(GL_COLOR_BUFFER_BIT)

            sceneManager.loop(inputManager, timeManager.update().delta)

            glfwSwapBuffers(window)
            inputManager.update()
            glfwPollEvents()
        }
    }
}