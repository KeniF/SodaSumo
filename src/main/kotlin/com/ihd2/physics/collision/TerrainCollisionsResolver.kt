package com.ihd2.physics.collision

import com.ihd2.model.Mass
import com.ihd2.model.Model
import com.ihd2.model.Terrain
import com.ihd2.physics.PhysicsConfig
import org.dyn4j.geometry.Segment
import org.dyn4j.geometry.Vector2
import java.awt.geom.Line2D

class TerrainCollisionsResolver {

    companion object {
        private val MASS_LINE by lazy { Line2D.Double() }
        private val Y_AXIS = Vector2(0.0, 1.0)

        fun resolveCollisions(model: Model, terrain: Terrain, config: PhysicsConfig) {
            val collided = HashMap<Mass, MutableList<Line2D>>()
            for (mass in model.masses) {
                mass.apply {
                    MASS_LINE.setLine(
                        lastPosition.x,
                        lastPosition.y,
                        position.x,
                        position.y)
                }
                for (line in terrain.lines) {
                    if (line.intersectsLine(MASS_LINE)) {
                        if (!collided.containsKey(mass)) {
                            collided[mass] = mutableListOf(line)
                        } else {
                            collided[mass]!!.add(line)
                        }
                    }
                }
            }
            for ((mass, lineList) in collided) {
                if (lineList.size == 1) {
                    val projectedPoint = Segment.getPointOnLineClosestToPoint(
                        mass.position,
                        Vector2(lineList[0].x1, lineList[0].y1),
                        Vector2(lineList[0].x2, lineList[0].y2))
                    val currentPointToProjected = projectedPoint.difference(mass.position)
                    val angleFromY = currentPointToProjected.getAngleBetween(Y_AXIS)

                    mass.apply {
                        velocity.inverseRotate(angleFromY)
                        velocity.x *= config.surfaceFriction
                        velocity.y = velocity.yComponent.negate().y * config.surfaceReflection
                        velocity.rotate(angleFromY)
                        currentPointToProjected.magnitude = 0.001
                        projectedPoint.add(currentPointToProjected)
                        position.x = projectedPoint.x
                        position.y = projectedPoint.y
                    }
                    model.adjustBoundRect(mass)
                } else {
                    for (line in lineList) {
                        // TODO Multiple terrain hits for 1 mass
                    }
                }
            }
        }
    }
}