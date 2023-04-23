package resources

import java.io.File


/** Instructions for loading a shader. */
class ShaderLoadInstruction(
    /** The shader type. */
    val type: ShaderType,
    /** The system file path of the shader source code. */
    val path: String
)

/** Keeps track of labeled [ShaderPrograms][ShaderProgram]. */
class ShaderProgramRegistry(
    /** If true, default values from [ShaderProgram.DEFAULT] will be added to this registry. */
    addDefaults: Boolean = true
) : Registry<ShaderProgram, List<ShaderLoadInstruction>>() {

    companion object {
        /** The name of the default font shader program. */
        const val FONT_NAME = "quad"
    }

    init {
        if (addDefaults) ShaderProgram.DEFAULT.forEach { name -> set(name, ShaderProgram.DEFAULT[name]!!) }
    }

    override fun load(name: String, loadInstruction: List<ShaderLoadInstruction>) = set(
        name,
        ShaderProgram(buildList {
            loadInstruction.forEach { sli ->
                add(Shader(sli.type, File(sli.path).readText()))
            }
        })
    )
}