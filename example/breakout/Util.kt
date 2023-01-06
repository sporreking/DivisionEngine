package breakout

import com.curiouscreature.kotlin.math.Float2
import kotlin.math.sin
import kotlin.math.cos
import kotlin.math.PI

class Util {
    companion object {
        fun createCircle(radius: Float = 1f, center: Float2 = Float2(0f),
                         points: Int = 16, closed: Boolean = true) = (
            if (closed) 0..points else 0 until points
        ).map { i -> (2 * PI / points * i).let {
                    angle -> Float2(cos(angle).toFloat(), sin(angle).toFloat()) * radius + center
        }}
    }
}