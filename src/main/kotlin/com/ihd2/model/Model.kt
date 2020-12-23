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
    private var boundTop = -10000.0
    private var boundBottom = 10000.0
    var boundRight = -10000.0
        private set
    var boundLeft = 10000.0
        private set
    var name: String? = null

    fun addMass(m: Mass) {
        massMap[m.name] = m
        adjustBoundRect(m)
    }

    fun adjustBoundRect(mass: Mass) {
        boundLeft = min(boundLeft, mass.getX())
        boundRight = max(boundRight, mass.getX())
        boundTop = min(boundTop, mass.getY())
        boundBottom = max(boundBottom, mass.getY())
    }

    fun resetBoundRect() {
        boundTop = -10000.0
        boundBottom = 10000.0
        boundRight = -10000.0
        boundLeft = 10000.0
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
        springMap[s.name] = s
    }

    fun addMuscle(m: Muscle) {
        muscleMap[m.name] = m
    }

    override fun toString(): String {
        return """
            [Model] Masses:${massMap.size}
            Springs:${springMap.size}
            Muscles:${muscleMap.size}
            """.trimIndent()
    }
}