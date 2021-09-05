package ecs

abstract class ECSystem {
    /** Updates the specified [scene] using this system. */
    abstract fun update(scene: Scene)

    /** Renders the specified [scene] using this system. */
    abstract fun render(scene: Scene)
}