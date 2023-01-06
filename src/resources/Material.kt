package resources

import com.curiouscreature.kotlin.math.Float4

data class Material(
    /** The diffuse lighting component of this material. */
    var kd: Float = 1.0f,

    /** The specular lighting component of this material. */
    var ks: Float = .0f,

    /** The specular alpha exponent of this material. */
    var alpha: Float = 1.0f,

    /** The color of this material. */
    var color: Float4 = Float4(1.0f),

    /** The texture of this material. */
    var texture: Texture? = null
) {
    inline var r: Float get() = color.r; set(r) { color.r = r }
    inline var g: Float get() = color.g; set(g) { color.g = g }
    inline var b: Float get() = color.b; set(b) { color.b = b }
    inline var a: Float get() = color.a; set(a) { color.a = a }
    inline val useTexture: Boolean get() = texture != null
}