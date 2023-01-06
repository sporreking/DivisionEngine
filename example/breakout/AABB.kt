package breakout

import com.curiouscreature.kotlin.math.Float2

data class IntersectionInfo(
    val other: AABB,
    val depthLeft: Float,
    val depthBottom: Float,
    val depthRight: Float,
    val depthTop: Float
) {
    val above = depthTop <= 0
    val below = depthBottom <= 0
    val rightOf = depthRight <= 0
    val leftOf = depthLeft <= 0

    val intersecting = !above && ! below && !rightOf && ! leftOf


}

class AABB(
    val position: Float2,
    val dimensions: Float2
) {
    val left get() = position.x - dimensions.x / 2
    val right get() = position.x + dimensions.x / 2
    val top get() = position.y + dimensions.y / 2
    val bottom get() = position.y - dimensions.y / 2

    fun intersection(other: AABB) = IntersectionInfo(
        other,
        depthLeft = intersectionDepth(other, Placement.LEFT),
        depthBottom = intersectionDepth(other, Placement.BOTTOM),
        depthRight = intersectionDepth(other, Placement.RIGHT),
        depthTop = intersectionDepth(other, Placement.TOP)
    )

    private fun intersectionDepth(other: AABB, otherSide: Placement) = when(otherSide) {
        Placement.LEFT -> right - other.left
        Placement.BOTTOM -> top - other.bottom
        Placement.RIGHT -> other.right - left
        Placement.TOP -> other.top - bottom
    }

    infix fun below(other: AABB) = top < other.bottom
    infix fun above(other: AABB) = bottom > other.top
    infix fun leftOf(other: AABB) = right < other.left
    infix fun rightOf(other: AABB) = left > other.right
}

// TODO: REMOVE OLD SHIT
// fun intersects(other: AABB) = other.left < right && other.right > left && other.bottom < top && other.top > bottom