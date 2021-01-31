package com.ihd2.physics

import com.ihd2.model.Mass
import com.ihd2.model.Model
import com.ihd2.model.Muscle
import kotlin.math.*

class SpringAccelerator {

    companion object {
        fun accelerateAndMove(model: Model, config: PhysicsConfig) {
            accelerateSpringsAndMuscles(model)
            moveMasses(model, config)
        }

        private fun accelerateSpringsAndMuscles(model: Model) {
            accelerateSprings(model, false)
            accelerateSprings(model, true)
        }

        private fun accelerateSprings(model: Model, isMuscle: Boolean) {
            val springs = if (isMuscle) model.muscles else model.springs
            for (spring in springs) {
                var restLength: Double
                var mass1: Mass
                var mass2: Mass

                if (isMuscle) {
                    val muscle = spring as Muscle
                    val amp = abs(muscle.amplitude)
                    val phase = muscle.phase
                    val rLength = muscle.restLength
                    // new = old * (1.0 + waveAmplitude * muscleAmplitude * sine())
                    // * 2 pi to convert to radians
                    // - wavePhase to set correct restLength of Muscle
                    restLength = rLength * (1.0 + model.waveAmplitude * amp *
                            sin((model.waveSpeed * model.noOfFrames + phase - model.wavePhase) * 2.0 * Math.PI))
                    mass1 = muscle.mass1
                    mass2 = muscle.mass2
                } else {
                    restLength = spring.restLength
                    mass1 = spring.mass1
                    mass2 = spring.mass2
                }

                val mass1X = mass1.position.x
                val mass1Y = mass1.position.y
                val mass2X = mass2.position.x
                val mass2Y = mass2.position.y
                val lengthX = abs(mass1X - mass2X) //absolute value, so angle is always +
                val lengthY = abs(mass1Y - mass2Y)
                val length = sqrt(lengthX * lengthX + lengthY * lengthY) //Pythagoras'
                val extension = length - restLength

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
                //damping for F=-fv
                val oldVx = mass.velocity.x
                val oldVy = mass.velocity.y
                var newVx = oldVx + mass.acceleration.x
                newVx -= newVx * model.friction
                var newVy = oldVy + mass.acceleration.y
                newVy -= newVy * model.friction
                newVy -= model.gravity
                newVy = newVy.coerceIn(-config.speedLimit, config.speedLimit)
                newVx = newVx.coerceIn(-config.speedLimit, config.speedLimit)
                val oldPx = mass.position.x
                val oldPy = mass.position.y
                val newPx = oldPx + newVx
                var newPy = oldPy + newVy

                //if goes through ground
                if (newPy <= config.groundHeight) {
                    if (newVy < 0) newVy *= config.surfaceReflection
                    newPy = config.groundHeight
                    newVx *= config.surfaceFriction
                }
                mass.setVx(newVx)
                mass.setVy(newVy)
                mass.setX(newPx)
                mass.setY(newPy)
                model.adjustBoundRect(mass)
                mass.clearAccelerations()
            }
        }
    }
}