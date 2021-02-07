package com.ihd2.physics

import com.ihd2.graphics.GraphicsRenderer
import com.ihd2.model.Scene
import com.ihd2.model.Scene.Companion.EMPTY_SCENE

class PhysicalWorld {

    var gameFrames = 0.0
        private set
    private var scene: Scene = EMPTY_SCENE
    private lateinit var config: PhysicsConfig

    var firstCollisionInfo: CollisionInfo = CollisionInfo()

    fun generateNextFrame() {
        accelerateAndMoveSprings()
        resolveCollisions()
        scene.incrementTimeStep(STEP_SIZE)
        gameFrames += STEP_SIZE
    }

    fun reset(scene: Scene, config: PhysicsConfig, width: Int) {
        scene.moveToStartPosition(width)
        this.scene = scene
        this.config = config
        gameFrames = 0.0
        firstCollisionInfo.reset()
    }

    fun render(renderer: GraphicsRenderer) {
        scene.render(renderer)
    }

    private fun accelerateAndMoveSprings() {
        SpringAccelerator.accelerateAndMove(scene.leftModel, config)
        SpringAccelerator.accelerateAndMove(scene.rightModel, config)
    }

    private fun resolveCollisions() {
        val collisionInfo = CollisionsResolver.resolveCollisions(
            scene.leftModel,
            scene.rightModel,
            config)

        if (!firstCollisionInfo.collided && collisionInfo.collided) {
            firstCollisionInfo = collisionInfo
        }
    }

    companion object {
        // TODO: Pass this into SpringAccelerator and CollisionChecker
        private const val STEP_SIZE = 1.0
    }
}