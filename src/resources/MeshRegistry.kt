package resources

class MeshRegistry : Registry<Mesh, String>() {

    companion object {
        /** The name of the default quad mesh. */
        const val QUAD_NAME = "quad"
    }

    // Add default meshes
    init {
        set(QUAD_NAME, Mesh.QUAD)
    }

    // TODO: Implement object loader
    override fun load(name: String, loadInstruction: String): Mesh? = null
}