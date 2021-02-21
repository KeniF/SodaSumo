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
        private val LINE_LAST by lazy { Line2D.Double() }
        private val LINE_CURRENT by lazy { Line2D.Double() }
        private val MASS_LINE by lazy { Line2D.Double() }
        private val Y_AXIS = Vector2(0.0, 1.0)

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
                    MASS_LINE.setLine(position.x, position.y, lastPosition.x, lastPosition.y)
                }
                spring.apply {
                    LINE_LAST.setLine(
                        mass1.lastPosition.x,
                        mass1.lastPosition.y,
                        mass2.lastPosition.x,
                        mass2.lastPosition.y)
                    LINE_CURRENT.setLine(
                        mass1.position.x,
                        mass1.position.y,
                        mass2.position.x,
                        mass2.position.y)
                }
                var collided = false
                if (LINE_CURRENT.intersectsLine(MASS_LINE)) {
                    collided = true
                    logCollision("a", logger, isLeftReferenceMass, mass, spring)
                } else if (LINE_LAST.intersectsLine(MASS_LINE)) {
                    collided = true
                    logCollision("b", logger, isLeftReferenceMass, mass, spring)
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
                        logCollision("c", logger, isLeftReferenceMass, mass, spring)
                    }
                }
                if (collided) {
                    collisionResponse(mass, spring, config)
                    saveCollisionInfo(massesToRevert, info, mass, spring)
                }
            }
        }

        private fun logCollision(
            type: String,
            logger: Logger,
            isLeftReferenceMass: Boolean,
            mass: Mass,
            spring: Spring
        ) {
            if (isLeftReferenceMass) {
                logger.d("$type ${mass.id} -> ${spring.mass1.id}--${spring.mass2.id}")
            } else {
                logger.d("$type ${spring.mass1.id}--${spring.mass2.id} <- ${mass.id}")
            }
        }

        private fun collisionResponse(mass: Mass, spring: Spring, config: PhysicsConfig) {
            val vector12 = Vector2(spring.mass2.lastPosition).difference(Vector2(spring.mass1.lastPosition))
            val angleFromYAxis = Y_AXIS.getAngleBetween(vector12)
            spring.apply {
                mass.velocity.rotate(angleFromYAxis)
                mass1.velocity.rotate(angleFromYAxis)
                mass2.velocity.rotate(angleFromYAxis)
                val speedX = sqrt(
                    (
                            (mass.velocity.x * mass.velocity.x +
                                    mass1.velocity.x * mass1.velocity.x +
                                    mass2.velocity.x * mass2.velocity.x) / 3.0 * config.energyLeft
                            )
                )
                mass.velocity.x = 0.0
                mass1.velocity.x = 0.0
                mass2.velocity.x = 0.0

                // Keep the component parallel to the spring
                mass.velocity.inverseRotate(angleFromYAxis)
                mass1.velocity.inverseRotate(angleFromYAxis)
                mass2.velocity.inverseRotate(angleFromYAxis)

                val pointOnSpringClosestToMass = Segment.getPointOnLineClosestToPoint(
                    mass.lastPosition,
                    mass1.lastPosition,
                    mass2.lastPosition
                )
                val massToLine = pointOnSpringClosestToMass.difference(mass.lastPosition)

                val accelerationPerpendicularToSpring = Vector2.create(speedX, massToLine.direction)

                mass1.apply {
                    accelerate(accelerationPerpendicularToSpring)
                    accelerate(velocity)
                    velocity.zero()
                }
                mass2.apply {
                    accelerate(accelerationPerpendicularToSpring)
                    accelerate(velocity)
                    velocity.zero()
                }
                mass.apply {
                    accelerate(accelerationPerpendicularToSpring.negate())
                    accelerate(velocity)
                    velocity.zero()
                }
            }
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