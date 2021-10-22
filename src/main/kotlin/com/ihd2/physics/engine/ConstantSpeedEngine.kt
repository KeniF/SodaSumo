package com.ihd2.physics.engine

import com.ihd2.model.Model
import com.ihd2.physics.PhysicsConfig

class ConstantSpeedEngine(private val speed: Double): Engine {
    override fun move(model: Model, config: PhysicsConfig) {
        model.resetBoundRect()
        for (mass in model.masses) {
            mass.apply {
                clearAccelerations()
                if (!model.flipModel ) {
                    setPosition(mass.position.x + speed, mass.position.y)
                    setVelocity(speed, 0.0)
                } else {
                    setPosition(mass.position.x - speed, mass.position.y)
                    setVelocity(-speed, 0.0)
                }
            }
            model.adjustBoundRect(mass)
        }
    }
}