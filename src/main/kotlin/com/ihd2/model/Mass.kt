package com.ihd2.model

import com.ihd2.graphics.GraphicsRenderer
import com.ihd2.graphics.Renderable
import org.dyn4j.geometry.Vector2
import java.awt.Color

class Mass(val id: Int): Renderable {
    val acceleration = Vector2()
    val position = Vector2()
    val lastPosition = Vector2()
    val velocity = Vector2()
    val lastVelocity = Vector2()

    fun setX(x: Double) {
        lastPosition.x = position.x
        position.x = x
    }

    fun setY(y: Double) {
        lastPosition.y = position.y
        position.y = y
    }

    fun setVx(vx: Double) {
        lastVelocity.x = velocity.x
        velocity.x = vx
    }

    fun setVy(vy: Double) {
        lastVelocity.y = velocity.y
        velocity.y = vy
    }

    fun revertPoints() {
        position.x = lastPosition.x
        position.y = lastPosition.y
    }

    fun accelerate(vector: Vector2) {
        acceleration.add(vector)
    }

    fun clearAccelerations() {
        acceleration.zero()
    }

    override fun render(renderer: GraphicsRenderer) {
        val color = when (renderer.isDebug()) {
            true -> Color.GRAY
            false -> Color.BLACK
        }
        renderer.drawEllipse(color, position.x, position.y, MASS_DIAMETER, MASS_DIAMETER)

        renderer.drawDebugText(
            Color.BLACK,
            position.x.toInt(),
            position.y.toInt(),
            "$id")
    }

    override fun toString(): String {
        return "$id Vx:$velocity.x Vy:$velocity.y X:$position.x Y:$position.y"
    }

    companion object {
        const val MASS_DIAMETER = 4.0
    }
}