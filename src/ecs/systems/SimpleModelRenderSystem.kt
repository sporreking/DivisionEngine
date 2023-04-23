package ecs.systems

import ecs.Camera
import ecs.ECSystem
import ecs.Scene
import ecs.components.ModelComponent
import io.InputManager
import org.lwjgl.opengl.GL11.*

/**
 * Renders [ModelComponets][ModelComponent] in the scenes using their respective [meshes][resources.Mesh],
 * [materials][resources.Material], and [shaders][resources.ShaderProgram].
 */
class SimpleModelRenderSystem : ECSystem() {
    override fun update(scene: Scene, inputManager: InputManager, delta: Double) = Unit

    override fun render(scene: Scene) {
        scene.getComponents<ModelComponent>().values.forEach { c ->

            // Send camera
            c.shaderProgram.sendCamera(scene.camera!!)

            // Send model transform
            c.shaderProgram.sendModelTransform(c.parent!!.transform, bind = false)

            // Send material and bind shader program
            c.shaderProgram.sendMaterial(c.material, bind = false)

            // Bind mesh
            c.mesh.bind()

            // Draw mesh
            if (c.mesh.indexBufferObject == null) glDrawArrays(c.mesh.mode, 0, c.mesh.numVertices)
            else glDrawElements(c.mesh.mode, c.mesh.numIndices,  GL_UNSIGNED_INT, 0)
        }
    }
}