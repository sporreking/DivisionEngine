package ecs

import com.curiouscreature.kotlin.math.Float3

data class Transform(
    /** The position of this transform. */
    val position: Float3 = Float3(),

    /** The scale of this transform. */
    val scale: Float3 = Float3(1.0f, 1.0f, 1.0f),

    /** The rotation of this transform. */
    val rotation: Float3 = Float3()
) : Component() {
    inline var px: Float get() = position.x; set(x) { position.x = x }
    inline var py: Float get() = position.y; set(y) { position.y = y }
    inline var pz: Float get() = position.z; set(z) { position.z = z }

    inline var sx: Float get() = scale.x; set(x) { scale.x = x }
    inline var sy: Float get() = scale.y; set(y) { scale.y = y }
    inline var sz: Float get() = scale.z; set(z) { scale.z = z }

    inline var rx: Float get() = rotation.x; set(x) { rotation.x = x }
    inline var ry: Float get() = rotation.y; set(y) { rotation.y = y }
    inline var rz: Float get() = rotation.z; set(z) { rotation.z = z }
}