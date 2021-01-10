package com.ihd2.model

import com.ihd2.graphics.GraphicsRenderer
import com.ihd2.graphics.Renderable
import java.lang.Double.max
import java.lang.Double.min

class Model: Renderable {
    val masses = HashSet<Mass>()
    val springs = HashSet<Spring>()
    val muscles = HashSet<Muscle>()
    var friction = 0.0
    var gravity = 0.0
    var springyness = 0.0
    var wavePhase = 0.0
    var waveSpeed = 0.0
    var waveAmplitude = 0.0
    var noOfFrames = 0
        private set
    private var boundTop = Double.NEGATIVE_INFINITY
    private var boundBottom = Double.POSITIVE_INFINITY
    var boundRight = Double.NEGATIVE_INFINITY
        private set
    var boundLeft = Double.POSITIVE_INFINITY
        private set
    var name: String? = null

    fun addMass(m: Mass) {
        masses.add(m)
        adjustBoundRect(m)
    }

    fun adjustBoundRect(mass: Mass) {
        boundLeft = min(boundLeft, mass.getX())
        boundRight = max(boundRight, mass.getX())
        boundTop = max(boundTop, mass.getY())
        boundBottom = min(boundBottom, mass.getY())
    }

    fun resetBoundRect() {
        boundTop = Double.NEGATIVE_INFINITY
        boundBottom = Double.POSITIVE_INFINITY
        boundRight = Double.NEGATIVE_INFINITY
        boundLeft = Double.POSITIVE_INFINITY
    }

    val boundingRectangle: DoubleArray
        get() {
            val boundingRectangle = DoubleArray(4)
            boundingRectangle[0] = boundTop
            boundingRectangle[1] = boundBottom
            boundingRectangle[2] = boundLeft
            boundingRectangle[3] = boundRight
            return boundingRectangle
        }

    fun addSpring(s: Spring) {
        springs.add(s)
    }

    fun addMuscle(m: Muscle) {
        muscles.add(m)
    }

    fun step(forward: Boolean) {
        when (forward) {
            true -> noOfFrames += STEP_SIZE
            false -> noOfFrames -= STEP_SIZE
        }
    }

    fun shiftRight(shift: Double) {
        for (mass in masses) {
            mass.setX(mass.getX() + shift)
        }
    }

    override fun render(renderer: GraphicsRenderer) {
        for (mass in masses) {
            mass.render(renderer)
        }
        for (spring in springs) {
            spring.render(renderer)
        }
        for (muscle in muscles) {
            muscle.render(renderer)
        }
    }

    override fun toString(): String {
        return """
            [Model] Masses:${masses.size}
            Springs:${springs.size}
            Muscles:${muscles.size}
            """.trimIndent()
    }

    companion object {
        private const val STEP_SIZE = 1
    }
}