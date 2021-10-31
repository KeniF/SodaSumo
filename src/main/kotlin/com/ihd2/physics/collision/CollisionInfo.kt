package com.ihd2.physics.collision

data class CollisionInfo (
    var collided: Boolean = false,
    var collisionPoint: Double = 0.0
) {
    fun reset() {
        collided = false
        collisionPoint = 0.0
    }
}