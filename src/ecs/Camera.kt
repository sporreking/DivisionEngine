package ecs

import com.curiouscreature.kotlin.math.*

data class Camera(
    /** The projection matrix of this camera. */
    val projection: Mat4
) : Component() {

    companion object {
        /** Creates a new camera with an orthogonal [projection] matrix. */
        fun ortho(left: Float, right: Float, bottom: Float, top: Float, near: Float, far: Float)
            = Camera(ortho(l = left, r = right, b = bottom, t = top, n = near, f = far))

        /** Creates a new camera with a perspective [projection] matrix. */
        fun perspective(fieldOfView: Degrees, aspectRatio: Float, near: Float, far: Float)
                = Camera(perspective(fov = fieldOfView, aspect = aspectRatio, near = near, far = far))
    }

    inline val view get() = inverse(parent?.transform?.matrix ?: Mat4.identity())
}