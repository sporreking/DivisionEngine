package ecs.components

import ecs.Component
import resources.Material
import resources.Mesh
import resources.ShaderProgram

/**
 * Represents a renderable model. Requires [SimpleModelRenderSystem][ecs.systems.SimpleModelRenderSystem] or
 * another arbitrary model rendering system.
 */
data class ModelComponent(
    /** The mesh of this model. */
    val mesh: Mesh,

    /** The material of this model. */
    val material: Material,

    /** The shader program of this model. */
    val shaderProgram: ShaderProgram
) : Component()