package com.ihd2.physics

class CollisionInfo {
    var collided: Boolean = false
    var collisionPoint: Double = 0.0

    companion object {
        fun uncollided() = CollisionInfo()
    }
}