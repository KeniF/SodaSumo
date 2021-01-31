package com.ihd2.model

import com.ihd2.graphics.GraphicsRenderer
import com.ihd2.graphics.Renderable
import java.awt.Color

open class Spring(val id: Int): Renderable {
    lateinit var mass1: Mass
    lateinit var mass2: Mass
    var restLength = 0.0

    override fun render(renderer: GraphicsRenderer) {
        renderer.drawLine(
            Color.BLACK,
            mass1.position.x,
            mass1.position.y,
            mass2.position.x,
            mass2.position.y)
    }

    override fun toString(): String {
        return "$id a:$mass1 b:$mass2 restlength:$restLength"
    }
}