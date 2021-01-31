package com.ihd2.physics

class CollisionInfo private constructor(
    var collided: Boolean,
    var collisionPoint: Double = 0.0
) {
    companion object {
        val UNCOLLIDED = CollisionInfo(false, 0.0)
    }
}