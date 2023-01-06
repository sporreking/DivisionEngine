package ecs

import com.curiouscreature.kotlin.math.*

data class Transform(
    /** The position of this transform. */
    var position: Float3 = Float3(),

    /** The scale of this transform. */
    var scale: Float3 = Float3(1.0f, 1.0f, 1.0f),

    /** The orientation of this transform. */
    var orientation: Quaternion = Quaternion.identity()
) : Component() {
    inline var px: Float get() = position.x; set(x) { position.x = x }
    inline var py: Float get() = position.y; set(y) { position.y = y }
    inline var pz: Float get() = position.z; set(z) { position.z = z }

    inline var sx: Float get() = scale.x; set(x) { scale.x = x }
    inline var sy: Float get() = scale.y; set(y) { scale.y = y }
    inline var sz: Float get() = scale.z; set(z) { scale.z = z }

    inline var rx: Float get() = orientation.toEulerAngles().y
        set(x) { orientation = Quaternion.fromEulers(orientation.toEulerAngles().also { it.y = x }, 0f) }

    inline var ry: Float get() = orientation.z
        set(y) { orientation = Quaternion.fromEulers(orientation.toEulerAngles().also { it.z = y }, 0f) }

    inline var rz: Float get() = orientation.x
        set(z) { orientation = Quaternion.fromEulers(orientation.toEulerAngles().also { it.x = z }, 0f) }

    inline val forward get() = (Mat4.from(orientation) * Float4(0f, 0f, -1f, 1f)).xyz
    inline val up get() = (Mat4.from(orientation) * Float4(0f, 1f, 0f, 1f)).xyz

    inline val matrix: Mat4 get() = translation(position) * Mat4.from(orientation) * scale(scale)
}