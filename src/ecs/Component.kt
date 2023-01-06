package ecs

private var COMPONENT_ID_COUNTER = 0L

abstract class Component {
    /** The entity to which this component is attached, or `null` if is not attached to anything. */
    var parent: Entity? = null

    /** The unique [id] associated with this component instance. */
    val id = ++COMPONENT_ID_COUNTER

    /** Shorthand for getting the parent's transform. */
    val transform get() = parent?.transform
}