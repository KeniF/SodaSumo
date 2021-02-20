package com.ihd2.physics

import com.ihd2.log.Logger
import com.ihd2.model.Mass
import com.ihd2.model.Model
import com.ihd2.model.Spring
import org.dyn4j.geometry.Segment
import org.dyn4j.geometry.Vector2
import java.awt.geom.Line2D
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.sqrt

class CollisionsResolver {

    companion object {
        private val horizontalLine by lazy { Line2D.Double() }
        private val lineLast by lazy { Line2D.Double() }
        private val lineCurrent by lazy { Line2D.Double() }
        private val massLine by lazy { Line2D.Double() }

        fun resolveCollisions(leftModel: Model, rightModel: Model, config: PhysicsConfig, logger: Logger): CollisionInfo {
            val collisionInfo = CollisionInfo()
            if (leftModel.boundRight < rightModel.boundLeft) return collisionInfo

            val massesToRevert = HashSet<Mass>()
            for (mass in leftModel.masses) {
                checkForSpringCollisions(mass, rightModel, massesToRevert, config, collisionInfo, true, logger)
            }
            for (mass in rightModel.masses) {
                checkForSpringCollisions(mass, leftModel, massesToRevert, config, collisionInfo, false, logger)
            }
            for (mass in massesToRevert) {
                mass.revertToLastPosition()
            }

            return collisionInfo
        }

        private fun checkForSpringCollisions(
            mass: Mass,
            model: Model,
            massesToRevert: MutableSet<Mass>,
            config: PhysicsConfig,
            info: CollisionInfo,
            isLeftReferenceMass: Boolean,
            logger: Logger
        ) {
            for (spring in model.springs) {
                mass.apply {
                    if (isLeftReferenceMass) {
                        horizontalLine.setLine(position.x, position.y, 10000.0, position.y)
                    } else {
                        horizontalLine.setLine(position.x, position.y, -10000.0, position.y)
                    }
                    massLine.setLine(position.x, position.y, lastPosition.x, lastPosition.y)
                }
                spring.apply {
                    lineLast.setLine(
                        mass1.lastPosition.x,
                        mass1.lastPosition.y,
                        mass2.lastPosition.x,
                        mass2.lastPosition.y)
                    lineCurrent.setLine(
                        mass1.position.x,
                        mass1.position.y,
                        mass2.position.x,
                        mass2.position.y)
                }
                var collided = false
                if (lineCurrent.intersectsLine(massLine)) {
                    collided = true
                    logger.d("a $isLeftReferenceMass / ${mass.id} + ${spring.mass1.id} + ${spring.mass2.id}")
                } else if (lineLast.intersectsLine(massLine)) {
                    collided = true
                    logger.d("b $isLeftReferenceMass / ${mass.id} + ${spring.mass1.id} + ${spring.mass2.id}")
                } else {
                    val pointOnSpringClosestToMass = Segment.getPointOnLineClosestToPoint(
                        mass.position,
                        spring.mass1.position,
                        spring.mass2.position
                    )
                    if (hasChangedSideOverTime(mass, spring) &&
                        hasChangedSideWithinSegment(
                            pointOnSpringClosestToMass,
                            spring,
                            logger) &&
                        !isTooFarToCollide(pointOnSpringClosestToMass, mass, spring)
                    ) {
                        collided = true
                        logger.d("c $isLeftReferenceMass / ${mass.id} + ${spring.mass1.id} + ${spring.mass2.id}")
                    }
                }
                if (collided) {
                    collisionResponse(mass, spring, config)
                    saveCollisionInfo(massesToRevert, info, mass, spring)
                }
            }
        }

        private fun collisionResponse(mass: Mass, spring: Spring, config: PhysicsConfig) {
            val pointOnSpringClosestToMass = Segment.getPointOnLineClosestToPoint(
                mass.lastPosition,
                spring.mass1.lastPosition,
                spring.mass2.lastPosition
            )
            val massToLine = pointOnSpringClosestToMass.difference(mass.lastPosition)
            val accelerationVector = Vector2.create(getVelocitySplit(mass, spring, config), massToLine.direction)
            spring.mass1.setVelocity(0.0, 0.0)
            spring.mass2.setVelocity(0.0, 0.0)
            mass.setVelocity(0.0, 0.0)
            //mass.position.set(pointOnLineClosestToMass.x, pointOnLineClosestToMass.y)
            spring.mass1.accelerate(accelerationVector)
            spring.mass2.accelerate(accelerationVector)

            mass.accelerate(accelerationVector.negate())
        }

        private fun hasChangedSideWithinSegment(
            pointOnSpringClosestToMass: Vector2,
            spring: Spring,
            logger: Logger
        ): Boolean {
            val dist1p = pointOnSpringClosestToMass.distance(spring.mass1.position)
            val dist2p = pointOnSpringClosestToMass.distance(spring.mass2.position)
            return abs(dist1p + dist2p - spring.mass1.position.distance(spring.mass2.position)) < 0.1
        }

        private fun isTooFarToCollide(
            pointOnSpringClosestToMass: Vector2,
            mass: Mass,
            spring: Spring
        ) : Boolean {
            val displacement1 = spring.mass1.position.distance(spring.mass1.lastPosition)
            val displacement2 = spring.mass2.position.distance(spring.mass2.lastPosition)
            return pointOnSpringClosestToMass.distance(mass.position) > max(displacement1, displacement2)
        }

        // 3-way split of the kinetic energy
        private fun getVelocitySplit(mass: Mass, spring: Spring, config: PhysicsConfig): Double {
            return sqrt((mass.lastVelocity.magnitudeSquared +
                    spring.mass1.lastVelocity.magnitudeSquared +
                    spring.mass2.lastVelocity.magnitudeSquared) / 3.0 * config.energyLeft)
        }

        private fun hasChangedSideOverTime(currentMass: Mass, spring: Spring): Boolean {
            val current = isMassOnLeftOfLine(currentMass.position, spring.mass1.position, spring.mass2.position)
            val last = isMassOnLeftOfLine(currentMass.lastPosition, spring.mass1.lastPosition, spring.mass2.lastPosition)
            return current * last == -1
        }

        private fun isMassOnLeftOfLine(massPosition: Vector2, position: Vector2, position2: Vector2): Int {
            val slope = (position.y - position2.y) / (position.x - position2.x)
            val yIntercept = position.y - slope * position.x
            val result = massPosition.y - slope * massPosition.x - yIntercept
            if (result == 0.0) return 0

            if (slope > 0 && result < 0) {
                return -1
            } else if (slope < 0 && result > 0) {
                return -1
            }
            return 1
        }

        private fun saveCollisionInfo(
            massesToRevert: MutableSet<Mass>,
            collisionInfo: CollisionInfo,
            mass: Mass,
            spring: Spring)
        {
            if (!collisionInfo.collided) {
                collisionInfo.collided = true
                collisionInfo.collisionPoint = mass.position.x
            }
            mass.hasCollided = true
            spring.hasCollided = true
            massesToRevert.add(mass)
            massesToRevert.add(spring.mass1)
            massesToRevert.add(spring.mass2)
        }
    }
}