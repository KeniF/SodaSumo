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
    var hasCollided = false

    fun setPosition(x: Double, y: Double) {
        lastPosition.x = position.x
        position.x = x
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

    override fun render(color: Color, renderer: GraphicsRenderer) {
        val colorToUse = when (renderer.isDebug()) {
            true -> if (hasCollided) Color.RED else Color.GRAY
            false -> color
        }
        renderer.drawEllipse(colorToUse, position.x, position.y, MASS_DIAMETER, MASS_DIAMETER)

        renderer.drawDebugText(
            Color.BLACK,
            position.x.toInt(),
            position.y.toInt(),
            "$id")
        hasCollided = false
    }

    companion object {
        const val MASS_DIAMETER = 4.0
    }
}