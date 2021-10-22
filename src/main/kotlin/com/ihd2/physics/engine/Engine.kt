package com.ihd2.physics.engine

import com.ihd2.model.Model
import com.ihd2.physics.PhysicsConfig

interface Engine {
    fun move(model: Model, config: PhysicsConfig)
}