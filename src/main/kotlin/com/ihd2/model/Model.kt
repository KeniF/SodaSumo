package com.ihd2.model

import com.ihd2.graphics.GraphicsRenderer
import com.ihd2.graphics.Renderable
import java.awt.Color
import java.lang.Double.max
import java.lang.Double.min

class Model: Renderable {
    val masses = LinkedHashSet<Mass>()
    val springs = LinkedHashSet<Spring>()
    var friction = 0.0
    var gravity = 0.0
    var springyness = 0.0
    var wavePhase = 0.0
    var waveSpeed = 0.0
    var waveAmplitude = 0.0
    var noOfFrames = 0.0
        private set
    private var boundTop = Double.NEGATIVE_INFINITY
    private var boundBottom = Double.POSITIVE_INFINITY

    @Volatile
    var flipModel: Boolean = false

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
        mass.position.apply {
            boundLeft = min(boundLeft, x)
            boundRight = max(boundRight, x)
            boundTop = max(boundTop, y)
            boundBottom = min(boundBottom, y)
        }
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

    fun incrementTimeStep(stepSize: Double) {
        when (!flipModel) {
            true -> noOfFrames += stepSize
            false -> noOfFrames -= stepSize
        }
    }

    fun shiftRight(shift: Double) {
        for (mass in masses) {
            mass.position.x += shift
        }
    }

    override fun render(color: Color, renderer: GraphicsRenderer) {
        for (mass in masses) {
            mass.render(color, renderer)
        }
        for (spring in springs) {
            spring.render(color, renderer)
        }
    }
}