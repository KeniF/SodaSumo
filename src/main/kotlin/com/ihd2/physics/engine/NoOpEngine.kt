package com.ihd2.physics.engine

import com.ihd2.model.Model
import com.ihd2.physics.PhysicsConfig

class NoOpEngine: Engine {
    override fun move(model: Model, config: PhysicsConfig) {
        // noop
    }
}