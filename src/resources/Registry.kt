package resources

abstract class Registry<T> {

    /** Map for internal resource storage. */
    private val resources = mutableMapOf<String, T>()

    /** Loads a resources from the given [path] and stores it at the specified [name]. */
    abstract fun load(name: String, path: String)

    /**
     * Stores the given [resource] at the specified [name].
     * @return the resource that was previously associated with the [name], or `null` if the [name] is new.
     */
    operator fun set(name: String, resource: T) = resources.put(name, resource)

    /** Gets the resource associated with the specified [name]. */
    operator fun get(name: String) = resources[name]
}