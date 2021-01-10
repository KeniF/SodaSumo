package com.ihd2.model

import com.ihd2.graphics.GraphicsRenderer
import com.ihd2.graphics.Renderable

class Scene(
    val leftModel: Model,
    val rightModel: Model) : Renderable
{

    fun incrementTimeStep() {
        leftModel.incrementTimeStep()
        rightModel.incrementTimeStep()
    }

    fun moveToStartPosition(width: Int) {
        val m1ShiftRight = width / 2.0 - leftModel.boundingRectangle[3] - 10.0
        leftModel.shiftRight(m1ShiftRight)

        val m2ShiftRight = width / 2.0 - rightModel.boundingRectangle[2] + 10.0
        rightModel.shiftRight(m2ShiftRight)
    }

    override fun render(renderer: GraphicsRenderer) {
        leftModel.render(renderer)
        rightModel.render(renderer)
    }
}