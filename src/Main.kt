import org.lwjgl.Version
import org.lwjgl.glfw.Callbacks.glfwFreeCallbacks
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.glfw.GLFWErrorCallback
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11.*
import org.lwjgl.system.MemoryStack.stackPush
import org.lwjgl.system.MemoryUtil.NULL


class Application {

    private var window = 0L

    /** Starts the application. */
    fun run() {
        println("LWJGL ${Version.getVersion()}")

        init()
        loop()

        // Free window callbacks and destroy window
        glfwFreeCallbacks(window)
        glfwDestroyWindow(window)

        // Terminate GLFW and free error callbacks
        glfwTerminate()
        glfwSetErrorCallback(null)?.free()
    }

    private fun init() {
        GLFWErrorCallback.createPrint(System.err).set()

        // Init GLFW
        check(glfwInit()) { "Unable to initialize GLFW" }

        // Configure GLFW
        glfwDefaultWindowHints()
        glfwWindowHint(GLFW_VISIBLE, GLFW_TRUE)
        glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE)

        // Create window
        window = glfwCreateWindow(800, 600, "ECS", NULL, NULL)
        check(window != NULL) { "Failed to create the GLFW window" }

        // Setup key callback
        glfwSetKeyCallback(window) { _, k, _, a, _ -> println("K: $k, A: $a") }

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
        glfwSwapInterval(1)
        glfwShowWindow(window)
    }

    private fun loop() {

        // Init GL
        GL.createCapabilities()
        glClearColor(1.0f, .0f, .0f, 1.0f)

        // Loop
        while (!glfwWindowShouldClose(window)) {
            glClear(GL_COLOR_BUFFER_BIT)

            glfwSwapBuffers(window)
            glfwPollEvents()
        }
    }

}

fun main() = Application().run()