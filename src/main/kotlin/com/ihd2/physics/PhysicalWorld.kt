package com.ihd2.physics

import com.ihd2.model.Scene

class PhysicalWorld(val scene: Scene, private val config: PhysicsConfig) {

    var gameFrames = 0
        private set

    var firstCollisionInfo: CollisionInfo? = null

    fun incrementTimeStep() {
        accelerateSprings()
        resolveCollisions()
        scene.incrementTimeStep()
        gameFrames++
    }

    private fun accelerateSprings() {
        SpringAccelerator.accelerateAndMove(scene.leftModel, config)
        SpringAccelerator.accelerateAndMove(scene.rightModel, config)
    }

    private fun resolveCollisions() {
        val collisionInfo = CollisionChecker.resolveCollisions(
            scene.leftModel,
            scene.rightModel,
            config)

        if (collisionInfo.collided && firstCollisionInfo == null) {
            firstCollisionInfo = collisionInfo
        }
    }
}