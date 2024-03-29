package com.ihd2.graphics

import java.awt.Color
import java.awt.geom.Line2D

interface GraphicsRenderer {
    fun drawLine(color: Color, x1: Double, y1: Double, x2: Double, y2: Double)
    fun drawLine(color: Color, line: Line2D)
    fun drawEllipse(color: Color, x: Double, y: Double, heightX: Double, heightY: Double)
    fun drawText(color: Color, x: Int, y: Int, message: String)
    fun drawDebugText(color: Color, x: Int, y: Int, message: String)
    fun isDebug() = false
}