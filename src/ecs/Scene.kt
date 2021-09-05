package ecs

class Scene(
    /** The systems to use for this scene, in sequential order. */
    private vararg val systems: ECSystem
) {

    private val components = mutableMapOf<Class<Component>, MutableMap<Long, Component>>()
    private val entities = mutableMapOf<Long, Entity>()

    /** Calls [ECSystem.update] for all systems in this scene, in sequential order. */
    fun update() = systems.forEach { system -> system.update(this) }

    /** Calls [ECSystem.render] for all systems in this scene, in sequential order. */
    fun render() = systems.forEach { system -> system.render(this) }

    /** Returns all components of the specified generic type from this scene. */
    inline fun <reified T : Component> getComponents() = getComponents(T::class.java) as Map<Long, T>

    /** Returns all components of the specified [type] from this scene. */
    fun getComponents(type: Class<*>) = components[type] ?: emptyMap()

    /** Adds the specified [component] to this scene. */
    internal fun addComponent(component: Component) {
        if (!components.containsKey(component.javaClass)) components[component.javaClass] = mutableMapOf()
        components[component.javaClass]!![component.id] = component
    }

    /** Removes the specified [component] from this scene. */
    internal fun removeComponent(component: Component) {
        components[component.javaClass]!!.remove(component.id)
    }

    /**
     * Adds the specified [entity] to this scene.
     * @throws IllegalArgumentException if an entity with the same ID already exists in this scene.
     */
    fun add(entity: Entity) {
        require(!entities.containsKey(entity.id))

        // Add entity
        entities[entity.id] = entity
        entity.scene = this

        // Add components
        entity.forEach(::addComponent)
    }

    /**
     * Removes the specified [entity] from this scene.
     * @throws IllegalArgumentException if the entity was not contained by this scene.
     */
    fun remove(entity: Entity) = remove(entity.id)

    /**
     * Removes the entity with the specified [id] from this scene.
     * @throws IllegalArgumentException if no entity with the specified [id] was contained by this scene.
     */
    fun remove(id: Long): Entity {
        require(entities.containsKey(id))

        entities[id]!!.forEach(::removeComponent)

        entities[id]!!.scene = null
        return entities.remove(id)!!
    }
}