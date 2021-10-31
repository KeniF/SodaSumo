package com.ihd2.model

import com.ihd2.graphics.GraphicsRenderer
import java.awt.Color

class Scene(
    val leftModel: Model,
    val rightModel: Model,
    val terrain: Terrain)
{

    fun incrementTimeStep(stepSize: Double) {
        leftModel.incrementTimeStep(stepSize)
        rightModel.incrementTimeStep(stepSize)
    }

    fun moveToStartPosition(width: Int) {
        val m1ShiftRight = width / 2.0 - leftModel.boundingRectangle[3] - 10.0
        leftModel.shiftRight(m1ShiftRight)
        leftModel.shiftUp(UP_SHIFT)

        val m2ShiftRight = width / 2.0 - rightModel.boundingRectangle[2] + 10.0
        rightModel.shiftRight(m2ShiftRight)
        rightModel.shiftUp(UP_SHIFT)
    }

    fun render(renderer: GraphicsRenderer) {
        terrain.render(Color.BLACK.brighter(), renderer)
        leftModel.render(if (renderer.isDebug()) Color.BLACK else Color.BLUE.darker(), renderer)
        rightModel.render(if (renderer.isDebug()) Color.BLACK else Color.RED.darker(), renderer)
    }

    companion object {
        val EMPTY_SCENE = Scene(Model(), Model(), Terrain())
        private const val UP_SHIFT = 70.0
    }
}