package com.ihd2.physics

import com.ihd2.DEBUG
import com.ihd2.graphics.GraphicsRenderer
import com.ihd2.log.Logger
import com.ihd2.log.NoopLogger
import com.ihd2.log.SystemLogger
import com.ihd2.model.Scene
import com.ihd2.model.Scene.Companion.EMPTY_SCENE
import com.ihd2.physics.engine.ConstantSpeedEngine
import com.ihd2.physics.engine.SpringEngine
import org.dyn4j.geometry.Vector2

class PhysicalWorld {

    var gameFrames = 0.0
        private set
    private var scene: Scene = EMPTY_SCENE
    private lateinit var config: PhysicsConfig
    private val logger: Logger = if (DEBUG) SystemLogger() else NoopLogger()
    private val springEngine = SpringEngine()
    private val horizontalEngine = ConstantSpeedEngine(Vector2(5.0, 0.0))

    var firstCollisionInfo: CollisionInfo = CollisionInfo()

    fun generateNextFrame() {
        logger.d("Frame $gameFrames >>>>>>>>>>>>>")
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
        springEngine.move(scene.leftModel, config)
        springEngine.move(scene.rightModel, config)
    }

    private fun resolveCollisions() {
        val collisionInfo = SpringCollisionsResolver.resolveCollisions(
            scene.leftModel,
            scene.rightModel,
            config,
            logger
        )

        if (!firstCollisionInfo.collided && collisionInfo.collided) {
            firstCollisionInfo = collisionInfo
        }
    }

    companion object {
        // TODO: Pass this into SpringAccelerator and CollisionChecker
        private const val STEP_SIZE = 1.0
    }
}