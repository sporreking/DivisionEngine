package ecs

private var ENTITY_ID_COUNTER = 0L

/**
 * Used for representing something with the game. An entity consists of a [Transform], and some arbitrary
 * [components][Component]. The transform provides spatial properties for the entity, while the components define
 * its remaining properties. Without components, an entity is practically an empty shell.
 */
class Entity(
    /** The initial [components] of this entity. */
    vararg components: Component
) : Iterable<Component> {

    /** The unique [id] associated with this entity instance. */
    val id = ++ENTITY_ID_COUNTER

    private val components = mutableMapOf<Long, Component>()

    /** The scene in which this entity is contained, or null if it has no scene. */
    var scene: Scene? = null

    /** Returns the first occurrence of a component of the specified generic type, or `null` if none were found. */
    inline fun <reified T : Component> get() = get(T::class.java) as T?

    /** Returns the first occurrence of a component of the specified [type], or `null` if none were found. */
    fun get(type: Class<*>) = components.values.find { c -> c.javaClass == type }

    /** Returns all components of the specified generic type, or `null` if none were found. */
    inline fun <reified T : Component> getAll() = getAll(T::class.java) as List<T>

    /** Returns all components of the specified [type], or `null` if none were found. */
    fun getAll(type: Class<*>) = components.values.filter { c -> c.javaClass == type  }

    /**
     * Adds a new [component] to this entity.
     * @throws IllegalArgumentException if a component with the same [id][Component.id] is already attached.
     */
    fun add(component: Component) {
        require(!components.containsKey(component.id)) { "Component with ID ${component.id} is already attached" }
        components[component.id] = component
        component.parent = this
        scene?.apply { addComponent(component) }
    }

    /**
     * Remove the specified [component], if it is attached to this entity.
     * @throws IllegalArgumentException if the component is not attached to this entity,
     * or if it is a [Transform].
     */
    fun remove(component: Component) = remove(component.id)

    /**
     * Removes the component associated with the specified [id].
     * @throws IllegalArgumentException if the [id] does not match a component,
     * or if it is associated with the transform.
     */
    fun remove(id: Long): Component {
        require(components.containsKey(id)) { "There was no component with ID $id" }
        require(id != transform.id) { "Tried to remove transform from entity with ID ${this.id}" }
        scene?.apply { removeComponent(components[id]!!) }
        components[id]!!.parent = null
        return components.remove(id)!!
    }

    /** Returns an iterator over the components of this object. */
    override fun iterator() = components.values.iterator()

    /** The [transform] of this entity. */
    val transform = Transform()

    init {
        add(transform)
        components.forEach(::add)
    }
}