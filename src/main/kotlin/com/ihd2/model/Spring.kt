package com.ihd2.model

import com.ihd2.graphics.GraphicsRenderer
import com.ihd2.graphics.Renderable
import java.awt.Color

open class Spring(val id: Int): Renderable {
    lateinit var mass1: Mass
    lateinit var mass2: Mass
    var restLength = 0.0
    var hasCollided = false
        set(value) {
            field = value
            mass1.hasCollided = value
            mass2.hasCollided = value
        }

    /**
     * Calculates the rest length at the current frame
     */
    open fun getRestLength(model: Model) = restLength

    override fun render(color: Color, renderer: GraphicsRenderer) {
        val colorToUse = when (renderer.isDebug()) {
            true -> if (hasCollided) Color.RED else color
            false -> color
        }
        renderer.drawLine(
            colorToUse,
            mass1.position.x,
            mass1.position.y,
            mass2.position.x,
            mass2.position.y)
        hasCollided = false
    }
}