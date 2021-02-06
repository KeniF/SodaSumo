package com.ihd2.model

import com.ihd2.graphics.GraphicsRenderer
import com.ihd2.graphics.Renderable
import java.awt.Color

open class Spring(val id: Int): Renderable {
    lateinit var mass1: Mass
    lateinit var mass2: Mass
    var restLength = 0.0

    /**
     * Calculates the rest length at the current frame
     */
    open fun getRestLength(model: Model) = restLength

    override fun render(color: Color, renderer: GraphicsRenderer) {
        renderer.drawLine(
            color,
            mass1.position.x,
            mass1.position.y,
            mass2.position.x,
            mass2.position.y)
    }
}