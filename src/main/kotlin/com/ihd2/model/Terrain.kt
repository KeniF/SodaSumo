package com.ihd2.model

import com.ihd2.graphics.GraphicsRenderer
import com.ihd2.graphics.Renderable
import java.awt.Color
import java.awt.geom.Line2D

class Terrain: Renderable {
    private val lines: MutableList<Line2D> = ArrayList(2)

    fun addLine(line: Line2D) {
        lines.add(line)
    }

    override fun render(color: Color, renderer: GraphicsRenderer) {
        for (line in lines) {
            renderer.drawLine(color, line)
        }
    }

    companion object {
        val BASIC_GROUND: Terrain by lazy {
            val terrain = Terrain()
            val line2D = Line2D.Double().apply {
                x1 = -100000.0
                y1 = 1.0
                x2 = 100000.0
                y2 = 1.0
            }
            terrain.addLine(line2D)
            terrain
        }
    }
}