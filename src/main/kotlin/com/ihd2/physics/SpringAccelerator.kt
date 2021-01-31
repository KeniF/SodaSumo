package com.ihd2.physics

import com.ihd2.model.Model
import com.ihd2.model.Spring
import kotlin.math.*

class SpringAccelerator {

    companion object {
        fun accelerateAndMove(model: Model, config: PhysicsConfig) {
            accelerateSprings(model, model.springs)
            accelerateSprings(model, model.muscles)
            moveMasses(model, config)
        }

        private fun accelerateSprings(model: Model, springs: Set<Spring>) {
            for (spring in springs) {
                val mass1 = spring.mass1
                val mass2 = spring.mass2
                val mass1X = mass1.position.x
                val mass1Y = mass1.position.y
                val mass2X = mass2.position.x
                val mass2Y = mass2.position.y
                val lengthX = abs(mass1X - mass2X) //absolute value, so angle is always +
                val lengthY = abs(mass1Y - mass2Y)
                val length = mass1.position.distance(mass2.position)
                val extension = length - spring.getRestLength(model)

                if (extension == 0.0) continue

                // Frictional force affects velocity only!!
                // F = kx = ma where m=1.0
                val resultantAcceleration = model.springyness * extension
                val angle = atan(lengthY / lengthX) //in radians
                val cosineAngle = cos(angle)
                val sineAngle = sin(angle)
                if (mass1X > mass2X) {
                    when {
                        mass2Y > mass1Y -> {
                            mass1.accelerate(
                                -(resultantAcceleration * cosineAngle),
                                resultantAcceleration * sineAngle
                            )
                            mass2.accelerate(
                                resultantAcceleration * cosineAngle,
                                -(resultantAcceleration * sineAngle)
                            )
                        }
                        mass2Y < mass1Y -> {
                            mass1.accelerate(
                                -(resultantAcceleration * cosineAngle),
                                -(resultantAcceleration * sineAngle)
                            )
                            mass2.accelerate(
                                resultantAcceleration * cosineAngle,
                                resultantAcceleration * sineAngle
                            )
                        }
                        else -> {
                            mass1.accelerate(-resultantAcceleration, 0.0)
                            mass2.accelerate(resultantAcceleration, 0.0)
                        }
                    }
                } else if (mass1X < mass2X) {
                    when {
                        mass2Y > mass1Y -> {
                            mass1.accelerate(
                                resultantAcceleration * cosineAngle,
                                resultantAcceleration * sineAngle
                            )
                            mass2.accelerate(
                                -(resultantAcceleration * cosineAngle),
                                -(resultantAcceleration * sineAngle)
                            )
                        }
                        mass2Y < mass1Y -> {
                            mass1.accelerate(
                                resultantAcceleration * cosineAngle,
                                -(resultantAcceleration * sineAngle)
                            )
                            mass2.accelerate(
                                -(resultantAcceleration * cosineAngle),
                                resultantAcceleration * sineAngle
                            )
                        }
                        else -> {
                            mass1.accelerate(resultantAcceleration, 0.0) //x
                            mass2.accelerate(-resultantAcceleration, 0.0) //x
                        }
                    }
                } else {
                    if (mass1Y > mass2Y) {
                        mass1.accelerate(0.0, -resultantAcceleration)
                        mass2.accelerate(0.0, resultantAcceleration)
                    } else if (mass1Y < mass2Y) {
                        mass1.accelerate(0.0, resultantAcceleration)
                        mass2.accelerate(0.0, -resultantAcceleration)
                    }
                }
            }
        }

        private fun moveMasses(model: Model, config: PhysicsConfig) {
            model.resetBoundRect()
            for (mass in model.masses) {
                mass.apply {
                    //damping for F=-fv
                    var newVx = velocity.x + acceleration.x
                    newVx -= newVx * model.friction
                    var newVy = velocity.y + acceleration.y
                    newVy -= newVy * model.friction
                    newVy -= model.gravity
                    newVy = newVy.coerceIn(-config.speedLimit, config.speedLimit)
                    newVx = newVx.coerceIn(-config.speedLimit, config.speedLimit)
                    val newPx = position.x + newVx
                    var newPy = position.y + newVy

                    //if goes through ground
                    if (newPy <= config.groundHeight) {
                        if (newVy < 0) newVy *= config.surfaceReflection
                        newPy = config.groundHeight
                        newVx *= config.surfaceFriction
                    }
                    setVx(newVx)
                    setVy(newVy)
                    setX(newPx)
                    setY(newPy)
                    model.adjustBoundRect(mass)
                    clearAccelerations()
                }
            }
        }
    }
}