package com.ihd2.model

import java.lang.Double.max
import java.lang.Double.min
import java.util.HashMap

class Model {
    val massMap = HashMap<Int, Mass>()
    val springMap = HashMap<Int, Spring>()
    val muscleMap = HashMap<Int, Muscle>()
    var friction = 0.0
    var gravity = 0.0
    var springyness = 0.0
    var wavePhase = 0.0
    var waveSpeed = 0.0
    var waveAmplitude = 0.0
    var noOfFrames = 0
    private var boundTop = Double.NEGATIVE_INFINITY
    private var boundBottom = Double.POSITIVE_INFINITY
    var boundRight = Double.NEGATIVE_INFINITY
        private set
    var boundLeft = Double.POSITIVE_INFINITY
        private set
    var name: String? = null

    fun addMass(m: Mass) {
        massMap[m.id] = m
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

    fun getMass(s: Int): Mass? {
        return massMap[s]
    }

    fun getSpring(s: Int): Spring? {
        return springMap[s]
    }

    fun getMuscle(s: Int): Muscle? {
        return muscleMap[s]
    }

    fun addSpring(s: Spring) {
        springMap[s.id] = s
    }

    fun addMuscle(m: Muscle) {
        muscleMap[m.id] = m
    }

    override fun toString(): String {
        return """
            [Model] Masses:${massMap.size}
            Springs:${springMap.size}
            Muscles:${muscleMap.size}
            """.trimIndent()
    }
}