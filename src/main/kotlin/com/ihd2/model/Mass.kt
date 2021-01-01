package com.ihd2.model

import com.ihd2.dyn4j.SimulationBody
import org.dyn4j.geometry.Vector2

class Mass(val id: Int) {
    private var x = 0.0
    private var y = 0.0
    var simulationBody: SimulationBody? = null

    fun getX(): Double {
        if (simulationBody == null) return x
        return simulationBody!!.transform.translationX
    }

    fun setX(x: Double) {
        this.x = x
    }

    fun getY(): Double {
        if (simulationBody == null) return y
        return simulationBody!!.transform.translationY
    }

    fun setY(y: Double) {
        this.y = y
    }

    fun applyForce(x: Double, y: Double) {
        val direction = Vector2(x, y)
        val point = Vector2(this.x, this.y)
        simulationBody!!.applyForce(direction, point)
    }
}