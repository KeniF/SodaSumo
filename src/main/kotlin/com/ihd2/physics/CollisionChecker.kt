package com.ihd2.physics

import com.ihd2.model.Mass
import com.ihd2.model.Model
import com.ihd2.model.Spring
import java.awt.geom.Line2D
import kotlin.math.sqrt

class CollisionChecker {

    companion object {
        private val horizontalLine by lazy { Line2D.Double() }
        private val lineOld by lazy { Line2D.Double() }
        private val lineNew by lazy { Line2D.Double() }
        private val massLine by lazy { Line2D.Double() }

        fun resolveCollisions(leftModel: Model, rightModel: Model, config: PhysicsConfig): CollisionInfo {
            val collisionInfo = CollisionInfo()
            if (leftModel.boundRight < rightModel.boundLeft) return collisionInfo

            val massesToRevert = HashSet<Mass>()
            for (mass in leftModel.masses) {
                checkForSpringCollisionsLeft(mass, rightModel.springs, massesToRevert, config, collisionInfo)
                checkForSpringCollisionsLeft(mass, rightModel.muscles, massesToRevert, config, collisionInfo)
            }
            for (mass in rightModel.masses) {
                checkForSpringCollisionsRight(mass, leftModel.springs, massesToRevert, config, collisionInfo)
                checkForSpringCollisionsRight(mass, leftModel.muscles, massesToRevert, config, collisionInfo)
            }
            for (mass in massesToRevert) {
                mass.revertPoints()
            }

            return collisionInfo
        }

        // model2 reference mass
        private fun checkForSpringCollisionsRight(
            currentMass: Mass,
            springs: Set<Spring>,
            massesToRevert: MutableSet<Mass>,
            config: PhysicsConfig,
            info: CollisionInfo
        ) {
            val currentMassX = currentMass.getX()
            val currentMassY = currentMass.getY()
            for (spring in springs) {
                val springMass1 = spring.mass1
                val springMass2 = spring.mass2
                val springMass1x = springMass1.getX()
                val springMass2x = springMass2.getX()
                val springMass1y = springMass1.getY()
                val springMass2y = springMass2.getY()
                if (currentMassX > springMass1x && currentMassX > springMass2x) { //not collided
                    //prune
                } else if (springMass1x != springMass2x && springMass1y != springMass2y) { // not vertical / horizontal
                    var slopeOfLine = (springMass1y - springMass2y) / (springMass1x - springMass2x)
                    val currentMassOldX = currentMass.oldX
                    val currentMassOldY = currentMass.oldY
                    val yInterceptNew = springMass1y - slopeOfLine * springMass1x
                    val resultNew = isLeftOfLine(currentMassX, currentMassY, yInterceptNew, slopeOfLine)
                    val mass1OldX = springMass1.oldX
                    val mass2OldX = springMass2.oldX
                    val mass1OldY = springMass1.oldY
                    val mass2OldY = springMass2.oldY
                    slopeOfLine = (mass1OldY - mass2OldY) / (mass1OldX - mass2OldX)
                    val yInterceptOld = mass1OldY - slopeOfLine * mass1OldX
                    val resultOld = isLeftOfLine(currentMassOldX, currentMassOldY, yInterceptOld, slopeOfLine)
                    horizontalLine.setLine(currentMassX, currentMassY, -10000.0, currentMassY)
                    lineNew.setLine(springMass1x, springMass1y, springMass2x, springMass2y)
                    lineOld.setLine(mass1OldX, mass1OldY, mass2OldX, mass2OldY)
                    massLine.setLine(currentMassX, currentMassY, currentMassOldX, currentMassOldY)
                    var countIntersections = 0
                    if (horizontalLine.intersectsLine(lineNew)) countIntersections++
                    if (horizontalLine.intersectsLine(lineOld)) countIntersections++
                    if (lineNew.intersectsLine(massLine) || lineOld.intersectsLine(massLine) ||
                        resultOld == -1 && resultNew == 1 && countIntersections == 1) {
                        val a = spring.mass1
                        val b = spring.mass2
                        if (!info.collided) {
                            info.collided = true
                            info.collisionPoint = currentMassX
                        }
                        massesToRevert.add(currentMass)
                        massesToRevert.add(a)
                        massesToRevert.add(b)
                        val aVx = a.oldVx
                        val bVx = b.oldVx
                        val aVy = a.oldVy
                        val bVy = b.oldVy
                        //lets [say] total (horizontal) kinetic energy is evenly distributed between 3 masses
                        val kineticEnergyX1 =
                            sqrt((aVx * aVx + bVx * bVx + currentMass.oldVx * currentMass.oldVx) / 3.0 * config.energyLeft)
                        val kineticEnergyY1 =
                            sqrt((aVy * aVy + bVy * bVy + currentMass.oldVy * currentMass.oldVy) / 3.0 * config.energyLeft)
                        currentMass.setVx(kineticEnergyX1)
                        a.setVx(0 - kineticEnergyX1)
                        b.setVx(0 - kineticEnergyX1)
                        if (slopeOfLine > 0) {
                            if (resultOld == -1) {
                                currentMass.setVy(0 - kineticEnergyY1)
                                a.setVy(kineticEnergyY1)
                                b.setVy(kineticEnergyY1)
                            } else {
                                currentMass.setVy(kineticEnergyY1)
                                a.setVy(0 - kineticEnergyY1)
                                b.setVy(0 - kineticEnergyY1)
                            }
                        } else { //slope<0
                            if (resultOld == -1) { //if on RHS
                                currentMass.setVy(kineticEnergyY1)
                                a.setVy(0 - kineticEnergyY1)
                                b.setVy(0 - kineticEnergyY1)
                            } else {
                                currentMass.setVy(0 - kineticEnergyY1)
                                a.setVy(kineticEnergyY1)
                                b.setVy(kineticEnergyY1)
                            }
                        }
                    }
                } else if (springMass1x == springMass2x) {
                    when {
                        currentMassX > springMass1x -> {
                        }
                        currentMassX > springMass1x - config.speedLimit -> {
                            val a = spring.mass1
                            val b = spring.mass2
                            if (!info.collided) {
                                info.collided = true
                                info.collisionPoint = currentMassX
                            }
                            massesToRevert.add(currentMass)
                            massesToRevert.add(a)
                            massesToRevert.add(b)
                            val aVx = a.oldVx
                            val bVx = b.oldVx
                            //lets [say] total (horizontal) kinetic energy is evenly distributed between 3 masses
                            val kineticEnergyX1 =
                                sqrt((aVx * aVx + bVx * bVx + currentMass.oldVx * currentMass.oldVx) / 3.0 * config.energyLeft)
                            currentMass.setVx(kineticEnergyX1)
                            a.setVx(0 - kineticEnergyX1)
                            b.setVx(0 - kineticEnergyX1)
                        }
                    }
                } else if (springMass1y == springMass2y) {
                    if (currentMassY > springMass1y) {
                        //no collision, pruned
                    } else if (currentMassY < springMass1y - config.speedLimit) {
                        val a = spring.mass1
                        val b = spring.mass2
                        if (!info.collided) {
                            info.collided = true
                            info.collisionPoint = currentMassX
                        }
                        massesToRevert.add(currentMass)
                        massesToRevert.add(a)
                        massesToRevert.add(b)
                        val aVy = a.oldVy
                        val bVy = b.oldVy
                        //lets [say] total (horizontal) kinetic energy is evenly distributed between 3 masses
                        val kineticEnergyY1 =
                            sqrt((aVy * aVy + bVy * bVy + currentMass.oldVy * currentMass.oldVy) / 3.0 * config.energyLeft)
                        currentMass.setVy(kineticEnergyY1)
                        a.setVy(0 - kineticEnergyY1)
                        b.setVy(0 - kineticEnergyY1)
                    }
                }
            }
        }

        private fun checkForSpringCollisionsLeft(
            currentMass: Mass,
            springs: Set<Spring>,
            massesToRevert: MutableSet<Mass>,
            config: PhysicsConfig,
            info: CollisionInfo
        ) {
            val currentMassX = currentMass.getX()
            val currentMassY = currentMass.getY()
            for (spring: Spring in springs) {
                val springMass1 = spring.mass1
                val springMass2 = spring.mass2
                val springMass1x = springMass1.getX()
                val springMass2x = springMass2.getX()
                val springMass1y = springMass1.getY()
                val springMass2y = springMass2.getY()
                if (currentMassX < springMass1x && currentMassX < springMass2x) {
                    //prune
                } else if (springMass1x != springMass2x && springMass1y != springMass2y) {
                    var slopeOfLine = (springMass1y - springMass2y) / (springMass1x - springMass2x)
                    val currentMassOldX = currentMass.oldX
                    val currentMassOldY = currentMass.oldY
                    val yInterceptNew = springMass1y - slopeOfLine * springMass1x
                    val resultNew = isLeftOfLine(currentMassX, currentMassY, yInterceptNew, slopeOfLine)
                    val mass1OldX = springMass1.oldX
                    val mass2OldX = springMass2.oldX
                    val mass1OldY = springMass1.oldY
                    val mass2OldY = springMass2.oldY
                    slopeOfLine = (mass1OldY - mass2OldY) / (mass1OldX - mass2OldX)
                    val yInterceptOld = mass1OldY - slopeOfLine * mass1OldX
                    val resultOld = isLeftOfLine(currentMassOldX, currentMassOldY, yInterceptOld, slopeOfLine)
                    horizontalLine.setLine(currentMassX, currentMassY, 10000.0, currentMassY)
                    lineNew.setLine(springMass1x, springMass1y, springMass2x, springMass2y)
                    lineOld.setLine(mass1OldX, mass1OldY, mass2OldX, mass2OldY)
                    massLine.setLine(currentMassX, currentMassY, currentMassOldX, currentMassOldY)
                    var countIntersections = 0
                    if (horizontalLine.intersectsLine(lineNew)) countIntersections++
                    if (horizontalLine.intersectsLine(lineOld)) countIntersections++
                    if (lineNew.intersectsLine(massLine) ||
                        lineOld.intersectsLine(massLine) ||
                        resultOld == 1 && resultNew == -1 && countIntersections == 1) {
                        if (!info.collided) {
                            info.collided = true
                            info.collisionPoint = currentMassX
                        }
                        val a = spring.mass1
                        val b = spring.mass2
                        massesToRevert.add(currentMass)
                        massesToRevert.add(a)
                        massesToRevert.add(b)
                        val aVx = a.oldVx
                        val bVx = b.oldVx
                        val aVy = a.oldVy
                        val bVy = b.oldVy
                        //lets [say] total (horizontal) kinetic energy is evenly distributed between 3 masses
                        val resultantVelocityX =
                            sqrt((aVx * aVx + bVx * bVx + currentMass.oldVx * currentMass.oldVx) / 3.0 * config.energyLeft)
                        val resultantVelocityY =
                            sqrt((aVy * aVy + bVy * bVy + currentMass.oldVy * currentMass.oldVy) / 3.0 * config.energyLeft)
                        currentMass.setVx(0 - resultantVelocityX)
                        a.setVx(resultantVelocityX)
                        b.setVx(resultantVelocityX)
                        if (slopeOfLine > 0) {
                            if (resultOld == 1) {
                                currentMass.setVy(resultantVelocityY)
                                a.setVy(0 - resultantVelocityY)
                                b.setVy(0 - resultantVelocityY)
                            } else {
                                currentMass.setVy(0 - resultantVelocityY)
                                a.setVy(resultantVelocityY)
                                b.setVy(resultantVelocityY)
                            }
                        } else {
                            if (resultOld == 1) {
                                currentMass.setVy(0 - resultantVelocityY)
                                a.setVy(resultantVelocityY)
                                b.setVy(resultantVelocityY)
                            } else {
                                currentMass.setVy(resultantVelocityY)
                                a.setVy(0 - resultantVelocityY)
                                b.setVy(0 - resultantVelocityY)
                            }
                        }
                    }
                } else if (springMass1x == springMass2x) {
                    when {
                        currentMassX < springMass1x -> {
                        }
                        currentMassX > springMass1x + config.speedLimit -> {
                            if (!info.collided) {
                                info.collided = true
                                info.collisionPoint = currentMassX
                            }
                            val a = spring.mass1
                            val b = spring.mass2
                            massesToRevert.add(currentMass)
                            massesToRevert.add(a)
                            massesToRevert.add(b)
                            val aVx = a.oldVx
                            val bVx = b.oldVx
                            //lets [say] total (horizontal) kinetic energy is evenly distributed between 3 masses
                            val kineticEnergyX1 =
                                sqrt((aVx * aVx + bVx * bVx + currentMass.oldVx * currentMass.oldVx) / 3.0 * config.energyLeft)
                            currentMass.setVx(0 - kineticEnergyX1)
                            a.setVx(kineticEnergyX1)
                            b.setVx(kineticEnergyX1)
                        }
                    }
                } else if (springMass1y == springMass2y) {
                    if (currentMassY > springMass1y) {
                        //no collision, pruned
                    } else if (currentMassY < springMass1y + config.speedLimit) {
                        val a = spring.mass1
                        val b = spring.mass2
                        if (!info.collided) {
                            info.collided = true
                            info.collisionPoint = currentMassX
                        }
                        massesToRevert.add(currentMass)
                        massesToRevert.add(a)
                        massesToRevert.add(b)
                        val aVy = a.oldVy
                        val bVy = b.oldVy
                        //lets [say] total (horizontal) kinetic energy is evenly distributed between 3 masses
                        val kineticEnergyY1 =
                            sqrt((aVy * aVy + bVy * bVy + currentMass.oldVy * currentMass.oldVy) / 3.0 * config.energyLeft)
                        currentMass.setVy(kineticEnergyY1)
                        a.setVy(0 - kineticEnergyY1)
                        b.setVy(0 - kineticEnergyY1)
                    }
                }
            }
        }

        private fun isLeftOfLine(x: Double, y: Double, yInter: Double, slope: Double): Int {
            //y-mx-c, returns 1 if on the left
            val result = y - slope * x - yInter
            if (result == 0.0) return 0

            if (slope > 0 && result < 0) {
                return -1
            } else if (slope < 0 && result > 0) {
                return -1
            }
            return 1
        }
    }
}