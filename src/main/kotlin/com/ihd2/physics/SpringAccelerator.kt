package com.ihd2.physics

import com.ihd2.model.Model
import com.ihd2.model.Spring
import org.dyn4j.geometry.Vector2

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
                val length = mass1.position.distance(mass2.position)
                val extension = length - spring.getRestLength(model)

                if (extension == 0.0) continue

                // F = kx = ma where m=1.0
                val resultantAcceleration = model.springyness * extension
                val vector2To1 = mass1.position.difference(mass2.position)
                val accelerationVector = Vector2.create(resultantAcceleration, vector2To1.direction)

                mass2.accelerate(accelerationVector)
                mass1.accelerate(accelerationVector.negate())
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
                    setPosition(newPx, newPy)
                    model.adjustBoundRect(mass)
                    clearAccelerations()
                }
            }
        }
    }
}