package com.ihd2.physics.engine

import com.ihd2.model.Model
import com.ihd2.physics.PhysicsConfig
import org.dyn4j.geometry.Vector2

class ConstantSpeedEngine(private val velocity: Vector2): Engine {
    override fun move(model: Model, config: PhysicsConfig) {
        model.resetBoundRect()
        for (mass in model.masses) {
            mass.apply {
                clearAccelerations()
                if (!model.flipModel ) {
                    setAndCacheLastPosition(mass.position.x + velocity.x, mass.position.y + velocity.y)
                    setAndCacheLastVelocity(velocity.x, velocity.y)
                } else {
                    setAndCacheLastPosition(mass.position.x - velocity.x, mass.position.y - velocity.y)
                    setAndCacheLastVelocity(-velocity.x, -velocity.y)
                }
            }
            model.adjustBoundRect(mass)
        }
    }
}