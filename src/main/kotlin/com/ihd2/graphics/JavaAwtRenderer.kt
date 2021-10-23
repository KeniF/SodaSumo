package com.ihd2.graphics

import java.awt.*
import java.awt.geom.Ellipse2D
import java.awt.geom.Line2D

class JavaAwtRenderer(private val isDebugging: Boolean): GraphicsRenderer {

    private val g2dEllipse = Ellipse2D.Double()
    private val g2dLine = Line2D.Double()
    private val debugFont = Font("Arial", Font.PLAIN, 9)
    private val resultFont = Font("Arial", Font.PLAIN, 20)

    private val renderingHints = RenderingHints(
        RenderingHints.KEY_ANTIALIASING,
        RenderingHints.VALUE_ANTIALIAS_ON
    )

    private lateinit var gfx2d: Graphics2D
    private var width: Int = 0
    private var height: Int = 0


    fun initFrame(graphics2D: Graphics2D, height: Int, width: Int) {
        gfx2d = graphics2D
        gfx2d.stroke = BasicStroke(LINE_WIDTH)
        gfx2d.setRenderingHints(renderingHints)
        this.height = height
        this.width = width
    }

    override fun drawLine(color: Color, x1: Double, y1: Double, x2: Double, y2: Double) {
        gfx2d.color = color
        g2dLine.setLine(x1, height - y1, x2, height - y2)
        gfx2d.draw(g2dLine)
    }

    override fun drawLine(color: Color, line: Line2D) {
        gfx2d.color = color
        g2dLine.setLine(line.x1, height - line.y1, line.x2, height - line.y2)
        gfx2d.draw(g2dLine)
    }

    override fun drawEllipse(color: Color, x: Double, y: Double, heightX: Double, heightY: Double) {
        gfx2d.color = color
        g2dEllipse.setFrame(
            x - heightX / 2,
            height - (y + heightY / 2),
            heightX,
            heightY)
        gfx2d.fill(g2dEllipse)
    }

    override fun drawText(color: Color, x: Int, y: Int, message: String) {
        gfx2d.color = color
        gfx2d.font = resultFont
        gfx2d.drawString(message, x, height - y)
    }

    override fun drawDebugText(color: Color, x: Int, y: Int, message: String) {
        if (isDebugging) {
            gfx2d.color = color
            gfx2d.font = debugFont
            gfx2d.drawString(message, x, height - y)
        }
    }

    override fun isDebug() = isDebugging

    companion object {
        private const val LINE_WIDTH = 0.6f
    }
}