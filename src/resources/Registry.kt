package resources

/** Wraps one instance of each registry. */
class RegistryKit {

    companion object {
        val DEFAULT = RegistryKit()
    }

    val shaderProgram = ShaderProgramRegistry()
    val texture = TextureRegistry()
    val mesh = MeshRegistry()
    val font = FontRegistry()
    val audioClip = AudioClipRegistry()
}

/** Used to keep track of labeled resources. */
abstract class Registry<T, L> : Iterable<String> {

    /** Map for internal resource storage. */
    var resources = mutableMapOf<String, T>()

    /** Loads a resources using the given [loadInstruction] and stores it at the specified [name]. */
    abstract fun load(name: String, loadInstruction: L): T?

    /**
     * Stores the given [resource] at the specified [name].
     * @return the resource that was previously associated with the [name], or `null` if the [name] is new.
     */
    operator fun set(name: String, resource: T) = resources.put(name, resource)

    /** Gets the resource associated with the specified [name]. */
    operator fun get(name: String) = resources[name]

    override fun iterator() = resources.keys.iterator()
}