package resources

import ecs.Camera
import ecs.Transform
import org.lwjgl.opengl.GL20.*

enum class ShaderType(
    /** The integer associated with the shader type. Corresponds to the shader type's underlying OpenGL constant. */
    val const: Int
) {
    // TODO: Add more shader types.
    VERTEX(GL_VERTEX_SHADER), FRAGMENT(GL_FRAGMENT_SHADER);
}

class Shader(
    /** The type of this shader. */
    val type: ShaderType,
    /** The source code to compile for this shader. */
    sourceCode: String
) {

    /** The OpenGL handle of this shader. */
    val handle = glCreateShader(type.const)

    init {
        // Load shader onto GPU and compile
        glShaderSource(handle, sourceCode)
        glCompileShader(handle)

        // Check for errors
        check(glGetShaderi(handle, GL_COMPILE_STATUS) != GL_FALSE) {
            "Error compiling shader of type ${type.name}:\n" +
            "-------------------------------\n" +
            glGetShaderInfoLog(handle) +
            "-------------------------------"
        }
    }
}

class ShaderProgram(
    /** The shaders to use for this shader program. */
    shaders: List<Shader>
) {

    companion object {
        // Attribute locations
        const val ATTLOC_POSITION = 0
        const val ATTLOC_NORMAL = 1
        const val ATTLOC_TEX_COORDS = 2
        const val ATTLOC_COLOR = 3

        const val ATTLOC_CUSTOM = 9000

        // Uniform location
        const val UNILOC_MODEL_MAT = 0
        const val UNILOC_VIEW_MAT = 1
        const val UNILOC_PROJECTION_MAT = 2

        const val UNILOC_NUM_LIGHTS = 1000

        const val UNILOC_MATERIAL_KD = 2000
        const val UNILOC_MATERIAL_KS = 2001
        const val UNILOC_MATERIAL_ALPHA = 2002
        const val UNILOC_MATERIAL_COLOR = 2003
        const val UNILOC_MATERIAL_TEXTURE = 2004

        const val UNILOC_USE_TEXTURE  = 3000

        const val UNILOC_CUSTOM = 9000

        /** The default shader program registry. */
        val DEFAULT = ShaderProgramRegistry(false).apply {
            load(ShaderProgramRegistry.FONT_NAME, listOf(
                ShaderLoadInstruction(ShaderType.VERTEX, "res/shaders/font.vert"),
                ShaderLoadInstruction(ShaderType.FRAGMENT, "res/shaders/font.frag")
            ))
        }
    }

    /** The OpenGL handle of this shader program. */
    val handle = glCreateProgram()

    init {
        // Attach shaders
        shaders.forEach { s ->
            glAttachShader(handle, s.handle)
        }

        // Link shader program
        glLinkProgram(handle)

        // Check linking errors
        check(glGetProgrami(handle, GL_LINK_STATUS) != GL_FALSE) {
            "Error linking shader program:\n" +
            "-------------------------------\n" +
            glGetProgramInfoLog(handle) +
            "-------------------------------"
        }

        // Validate shader program
        glValidateProgram(handle)

        // Check validation errors
        check(glGetProgrami(handle, GL_VALIDATE_STATUS) != GL_FALSE) {
            "Error validating shader program:\n" +
            "-------------------------------\n" +
            glGetProgramInfoLog(handle) +
            "-------------------------------"
        }

        // Detach shaders
        shaders.forEach { s ->
            glDetachShader(handle, s.handle)
        }
    }

    /** Binds this shader program. */
    fun bind() = glUseProgram(handle)

    /**
     * Forwards the properties of the given [material] to appropriate uniform locations and binds textures.
     *
     * If [bind] is set to true (default) the shader program will automatically be bound before the uniforms are sent.
     */
    fun sendMaterial(material: Material, bind: Boolean = true) = with (material) {
        // Bind shader program
        if (bind) this@ShaderProgram.bind()

        // Bind lighting properties
        glUniform1f(UNILOC_MATERIAL_KD, kd)
        glUniform1f(UNILOC_MATERIAL_KS, ks)
        glUniform1f(UNILOC_MATERIAL_ALPHA, alpha)

        // Bind color properties
        glUniform4f(UNILOC_MATERIAL_COLOR, r, g, b, a)

        // Bind texture properties
        texture?.also {
            glUniform1i(UNILOC_USE_TEXTURE, 1)
            glUniform1i(UNILOC_MATERIAL_TEXTURE, 0)
        }?.bind(0) ?: run {
            glUniform1i(UNILOC_USE_TEXTURE, 0)
        }
    }

    /**
     * Forwards the projection and view matrices of the given [camera] to the appropriate uniform locations.
     *
     * If [bind] is set to true (default) the shader program will automatically be bound before the uniforms are sent.
     */
    fun sendCamera(camera: Camera, bind: Boolean = true) {
        // Bind shader program
        if (bind) this@ShaderProgram.bind()

        // Bind projection matrix
        glUniformMatrix4fv(UNILOC_PROJECTION_MAT, false, camera.projection.asGLArray().toFloatArray())

        // Bind view matrix
        glUniformMatrix4fv(UNILOC_VIEW_MAT, false, camera.view.asGLArray().toFloatArray())
    }

    /**
     * Forwards the given model [transform] to the appropriate uniform location.
     *
     * If [bind] is set to true (default) the shader program will automatically be bound before the uniforms are sent.
     */
    fun sendModelTransform(transform: Transform, bind: Boolean = true) {
        // Bind shader program
        if (bind) this@ShaderProgram.bind()

        // Bind model matrix
        glUniformMatrix4fv(UNILOC_MODEL_MAT, false, transform.matrix.asGLArray().toFloatArray())
    }
}