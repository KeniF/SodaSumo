package com.ihd2.physics

import com.ihd2.model.Model

interface Accelerable {
    fun accelerateAndMove(model: Model, config: PhysicsConfig)
}