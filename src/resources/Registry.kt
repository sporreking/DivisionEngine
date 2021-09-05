package resources

abstract class Registry<T> {
    private val resources = mutableMapOf<String, T>()

    abstract fun load(name: String, path: String)

    fun put(name: String, resource: T) = resources.put(name, resource)
    operator fun get(name: String) = resources[name]
}