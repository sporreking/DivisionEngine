package breakout

import com.curiouscreature.kotlin.math.Float2
import com.curiouscreature.kotlin.math.Float3
import org.lwjgl.opengl.GL11
import resources.*

// TODO: DEFAULT RESOURCES

val breakoutRegistry = RegistryKit().apply {
    // Register textures
    with(texture) {
        load("test", "res/test.png")
    }

    // Register shader programs
    with(shaderProgram) {
        load("test", listOf(
            ShaderLoadInstruction(ShaderType.VERTEX, "res/shaders/test.vert"),
            ShaderLoadInstruction(ShaderType.FRAGMENT, "res/shaders/test.frag")
        ))
    }

    // Register meshes
    with(mesh) {
        set("ball", Mesh(
            listOf(Float2(0f, 0f), *Util.createCircle().toTypedArray()).let { points ->
                listOf(
                    AttributeData(
                        ShaderProgram.ATTLOC_POSITION,
                        points.map { Float3(it, 0f) }
                    ),
                    AttributeData(
                        ShaderProgram.ATTLOC_TEX_COORDS,
                        points
                    ),
                )
            },
            mode = GL11.GL_TRIANGLE_FAN
        ))
    }
}