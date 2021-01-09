package com.ihd2.model

import com.ihd2.graphics.GraphicsRenderer
import com.ihd2.graphics.Renderable
import java.awt.Color

class Muscle(id: Int) : Spring(id), Renderable {
    var amplitude = 0.0
    var phase = 0.0

    override fun render(renderer: GraphicsRenderer) {
        renderer.drawLine(
            Color.BLACK,
            mass1.getX(),
            mass1.getY(),
            mass2.getX(),
            mass2.getY())

        renderer.drawEllipse(
            Color.BLACK,
            (mass1.getX() + mass2.getX()) / 2.0,
            (mass1.getY() + mass2.getY()) / 2.0,
            MUSCLE_MARKER_SIZE,
            MUSCLE_MARKER_SIZE
        )
    }

    override fun toString(): String {
        return "$id a:$mass1 b:$mass2 amp:$amplitude phase:$phase restLeng:$restLength"
    }

    companion object {
        private const val MUSCLE_MARKER_SIZE = 3.0
    }
}