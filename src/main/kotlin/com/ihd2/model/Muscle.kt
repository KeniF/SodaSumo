package com.ihd2.model

import com.ihd2.graphics.GraphicsRenderer
import com.ihd2.graphics.Renderable
import java.awt.Color
import kotlin.math.sin

class Muscle(id: Int) : Spring(id), Renderable {
    var amplitude = 0.0
    var phase = 0.0

    /**
     * Calculates the rest length at the current frame
     */
    override fun getRestLength(model: Model): Double {
        // new = old * (1.0 + waveAmplitude * muscleAmplitude * sine())
        // * 2 pi to convert to radians
        // - wavePhase to set correct restLength of Muscle
        return restLength * (1.0 + model.waveAmplitude * amplitude *
                sin((model.waveSpeed * model.noOfFrames + phase - model.wavePhase) * 2.0 * Math.PI))
    }

    override fun render(renderer: GraphicsRenderer) {
        renderer.drawLine(
            Color.BLACK,
            mass1.position.x,
            mass1.position.y,
            mass2.position.x,
            mass2.position.y)

        renderer.drawEllipse(
            Color.BLACK,
            (mass1.position.x + mass2.position.x) / 2.0,
            (mass1.position.y + mass2.position.y) / 2.0,
            MUSCLE_MARKER_SIZE,
            MUSCLE_MARKER_SIZE
        )
    }

    companion object {
        private const val MUSCLE_MARKER_SIZE = 3.0
    }
}