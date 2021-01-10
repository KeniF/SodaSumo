package com.ihd2

import com.ihd2.graphics.GraphicsRenderer
import com.ihd2.graphics.JavaAwtRenderer
import javax.swing.JComponent
import java.awt.geom.Line2D
import java.awt.Graphics2D
import kotlin.jvm.Volatile
import java.awt.Graphics
import java.awt.Color
import com.ihd2.model.Mass
import com.ihd2.model.Model
import com.ihd2.model.Muscle
import com.ihd2.model.Spring
import java.lang.InterruptedException
import kotlin.math.*

class GameDraw: JComponent() {
    private var timeLimitMs = 15000L
    private val horizontalLine = Line2D.Double()
    private val lineOld = Line2D.Double()
    private val lineNew = Line2D.Double()
    private val massLine = Line2D.Double()

    private var physicsThread: Thread? = null
    private var gameFrames = 0
    private var model1: Model = Model()
    private var model2: Model = Model()
    private var firstContactPoint = 0.0
    private var collided = false
    private var resultMessage = ""
    private val renderer = JavaAwtRenderer(DEBUG)

    @Volatile
    private var invertM1 = false
    @Volatile
    private var invertM2 = false
    @Volatile
    var paused = false

    fun invertM1() {
        invertM1 = !invertM1
    }

    fun invertM2() {
        invertM2 = !invertM2
    }

    fun stepAnimationManually() {
        if (!run) return
        paused = true
        animateAndStep()
    }

    override fun paint(g: Graphics) {
        renderer.initFrame(g as Graphics2D, height - 2, width)
        model1.render(renderer)
        model2.render(renderer)

        drawVerticalLine(renderer)
        drawResult(renderer)
        drawDebugStats(renderer)
    }

    fun stop() {
        run = false
        paused = false
    }

    fun setTimeLimit(milliseconds: Long) {
        timeLimitMs = milliseconds
    }

    private fun drawVerticalLine(renderer: GraphicsRenderer) {
        if (!collided) return

        renderer.drawLine(
            Color.GRAY,
            firstContactPoint,
            0.0,
            firstContactPoint,
            1000.0)
    }

    private fun drawResult(renderer: GraphicsRenderer) {
        if (resultMessage == "") return

        renderer.drawText(
            Color.BLUE.darker().darker(),
            width / 2 - 150,
            248,
            resultMessage)
    }

    private fun drawDebugStats(renderer: GraphicsRenderer) {
        renderer.drawDebugText(Color.GRAY,
            2,
            288,
            "Frames: $gameFrames Frames_m1: ${model1.noOfFrames} Frames_m2: ${model2.noOfFrames}")
    }

    fun init() {
        run = true
        gameFrames = 0
        collided = false
        resultMessage = ""
    }

    fun provideModel1(model: Model) {
        run = false
        model1 = model
        val br = model1.boundingRectangle
        val shiftRight = width / 2.0 - br[3] - 10.0
        model1.shiftRight(shiftRight)
    }

    fun provideModel2(model: Model) {
        run = false
        model2 = model
        val br = model2.boundingRectangle
        val shiftRight = width / 2.0 - br[2] + 10.0
        model2.shiftRight(shiftRight)
    }

    fun startDraw() {
        physicsThread = Thread {
            val curThread = Thread.currentThread()
            var beforeRun = System.currentTimeMillis()
            while (curThread === physicsThread && run) {
                if (gameFrames > timeLimitMs / FRAME_DELAY) {
                    endGame()
                } else if (!paused) {
                    animateAndStep()
                }
                try {
                    //to keep a constant framerate depending on how far we are behind :D
                    beforeRun += FRAME_DELAY
                    Thread.sleep(max(0, beforeRun - System.currentTimeMillis()))
                } catch (e: InterruptedException) {
                    println(e)
                }
            }
        }
        physicsThread!!.start()
    }

    private fun animateAndStep() {
        animate()
        repaint()
        gameFrames++
        model1.step(forward = !invertM1)
        model2.step(forward = !invertM2)
    }

    private fun endGame() {
        run = false
        resultMessage(currentScore())
        repaint()
    }

    private fun resultMessage(score: Int): String {
        resultMessage = when {
            score == 0 -> {
                "Draw - They are equally good!"
            }
            score > 0 -> {
                model1.name + " wins! Score:" + score
            }
            else -> {
                model2.name + " wins! Score:" + -score
            }
        }
        return resultMessage
    }

    private fun currentScore(): Int {
        return ((model1.boundRight - firstContactPoint +
                model2.boundLeft - firstContactPoint) / 20).toInt()
    }

    private fun animate() {
        accelerateSpringsAndMuscles(model1)
        moveMasses(model1)
        accelerateSpringsAndMuscles(model2)
        moveMasses(model2)
        doCollision()
    }

    private fun accelerateSpringsAndMuscles(model: Model) {
        accelerateSprings(model, false)
        accelerateSprings(model, true)
    }

    private fun accelerateSprings(model: Model, isMuscle: Boolean) {
        val springs = if (isMuscle) model.muscles else model.springs
        for (spring in springs) {
            var restLength: Double
            var mass1: Mass
            var mass2: Mass

            if (isMuscle) {
                val muscle = spring as Muscle
                val amp = abs(muscle.amplitude)
                val phase = muscle.phase
                val rLength = muscle.restLength
                // new = old * (1.0 + waveAmplitude * muscleAmplitude * sine())
                // * 2 pi to convert to radians
                // - wavePhase to set correct restLength of Muscle
                restLength = rLength * (1.0 + model.waveAmplitude * amp *
                        sin((model.waveSpeed * model.noOfFrames + phase - model.wavePhase) * 2.0 * Math.PI))
                mass1 = muscle.mass1
                mass2 = muscle.mass2
            } else {
                restLength = spring.restLength
                mass1 = spring.mass1
                mass2 = spring.mass2
            }

            val mass1X = mass1.getX()
            val mass1Y = mass1.getY()
            val mass2X = mass2.getX()
            val mass2Y = mass2.getY()
            val lengthX = abs(mass1X - mass2X) //absolute value, so angle is always +
            val lengthY = abs(mass1Y - mass2Y)
            val length = sqrt(lengthX * lengthX + lengthY * lengthY) //Pythagoras'
            val extension = length - restLength

            if (extension == 0.0) continue

            // Frictional force affects velocity only!!
            // F = kx = ma where m=1.0
            val resultantAcceleration = model.springyness * extension
            val angle = atan(lengthY / lengthX) //in radians
            val cosineAngle = cos(angle)
            val sineAngle = sin(angle)
            if (mass1X > mass2X) {
                when {
                    mass2Y > mass1Y -> {
                        mass1.accelerate(
                            -(resultantAcceleration * cosineAngle),
                            resultantAcceleration * sineAngle
                        )
                        mass2.accelerate(
                            resultantAcceleration * cosineAngle,
                            -(resultantAcceleration * sineAngle)
                        )
                    }
                    mass2Y < mass1Y -> {
                        mass1.accelerate(
                            -(resultantAcceleration * cosineAngle),
                            -(resultantAcceleration * sineAngle)
                        )
                        mass2.accelerate(
                            resultantAcceleration * cosineAngle,
                            resultantAcceleration * sineAngle
                        )
                    }
                    else -> {
                        mass1.accelerate(-resultantAcceleration, 0.0)
                        mass2.accelerate(resultantAcceleration, 0.0)
                    }
                }
            } else if (mass1X < mass2X) {
                when {
                    mass2Y > mass1Y -> {
                        mass1.accelerate(
                            resultantAcceleration * cosineAngle,
                            resultantAcceleration * sineAngle
                        )
                        mass2.accelerate(
                            -(resultantAcceleration * cosineAngle),
                            -(resultantAcceleration * sineAngle)
                        )
                    }
                    mass2Y < mass1Y -> {
                        mass1.accelerate(
                            resultantAcceleration * cosineAngle,
                            -(resultantAcceleration * sineAngle)
                        )
                        mass2.accelerate(
                            -(resultantAcceleration * cosineAngle),
                            resultantAcceleration * sineAngle
                        )
                    }
                    else -> {
                        mass1.accelerate(resultantAcceleration, 0.0) //x
                        mass2.accelerate(-resultantAcceleration, 0.0) //x
                    }
                }
            } else {
                if (mass1Y > mass2Y) {
                    mass1.accelerate(0.0, -resultantAcceleration)
                    mass2.accelerate(0.0, resultantAcceleration)
                } else if (mass1Y < mass2Y) {
                    mass1.accelerate(0.0, resultantAcceleration)
                    mass2.accelerate(0.0, -resultantAcceleration)
                }
            }
        }
    }

    private fun moveMasses(model: Model) {
        model.resetBoundRect()
        for (mass in model.masses) {
            //damping for F=-fv
            val oldVx = mass.getVx()
            val oldVy = mass.getVy()
            var newVx = oldVx + mass.ax
            newVx -= newVx * model.friction
            var newVy = oldVy + mass.ay
            newVy -= newVy * model.friction
            newVy -= model.gravity
            newVy = newVy.coerceIn(-SPEED_LIMIT, SPEED_LIMIT)
            newVx = newVx.coerceIn(-SPEED_LIMIT, SPEED_LIMIT)
            val oldPx = mass.getX()
            val oldPy = mass.getY()
            val newPx = oldPx + newVx
            var newPy = oldPy + newVy

            //if goes through ground
            if (newPy <= GROUND_HEIGHT) {
                if (newVy < 0) newVy *= SURFACE_REFLECTION
                newPy = GROUND_HEIGHT
                newVx *= SURFACE_FRICTION
            }
            mass.setVx(newVx)
            mass.setVy(newVy)
            mass.setX(newPx)
            mass.setY(newPy)
            model.adjustBoundRect(mass)
            mass.clearAccelerations()
        }
    }

    private fun doCollision() {
        if (model1.boundRight < model2.boundLeft) return

        val massesToRevert = HashSet<Mass>()
        for (mass in model1.masses) {
            checkForSpringCollisions(mass, model2.springs, massesToRevert)
            checkForSpringCollisions(mass, model2.muscles, massesToRevert)
        }

        for (mass in model2.masses) {
            checkForSpringCollisionsRight(mass, model1.springs, massesToRevert)
            checkForSpringCollisionsRight(mass, model1.muscles, massesToRevert)
        }
        for (mass in massesToRevert) {
            mass.revertPoints()
        }
    }

    // model2 reference mass
    private fun checkForSpringCollisionsRight(currentMass: Mass, springs: Set<Spring>, massesToRevert: MutableSet<Mass>) {
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
                    if (!collided) {
                        collided = true
                        firstContactPoint = currentMassX
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
                        sqrt((aVx * aVx + bVx * bVx + currentMass.oldVx * currentMass.oldVx) / 3.0 * ENERGY_LEFT)
                    val kineticEnergyY1 =
                        sqrt((aVy * aVy + bVy * bVy + currentMass.oldVy * currentMass.oldVy) / 3.0 * ENERGY_LEFT)
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
                    currentMassX > springMass1x - SPEED_LIMIT -> {
                        val a = spring.mass1
                        val b = spring.mass2
                        if (!collided) {
                            collided = true
                            firstContactPoint = currentMassX
                        }
                        massesToRevert.add(currentMass)
                        massesToRevert.add(a)
                        massesToRevert.add(b)
                        val aVx = a.oldVx
                        val bVx = b.oldVx
                        //lets [say] total (horizontal) kinetic energy is evenly distributed between 3 masses
                        val kineticEnergyX1 =
                            sqrt((aVx * aVx + bVx * bVx + currentMass.oldVx * currentMass.oldVx) / 3.0 * ENERGY_LEFT)
                        currentMass.setVx(kineticEnergyX1)
                        a.setVx(0 - kineticEnergyX1)
                        b.setVx(0 - kineticEnergyX1)
                    }
                }
            } else if (springMass1y == springMass2y) {
                if (currentMassY > springMass1y) {
                    //no collision, pruned
                } else if (currentMassY < springMass1y - SPEED_LIMIT) {
                    val a = spring.mass1
                    val b = spring.mass2
                    if (!collided) {
                        collided = true
                        firstContactPoint = currentMassX
                    }
                    massesToRevert.add(currentMass)
                    massesToRevert.add(a)
                    massesToRevert.add(b)
                    val aVy = a.oldVy
                    val bVy = b.oldVy
                    //lets [say] total (horizontal) kinetic energy is evenly distributed between 3 masses
                    val kineticEnergyY1 =
                        sqrt((aVy * aVy + bVy * bVy + currentMass.oldVy * currentMass.oldVy) / 3.0 * ENERGY_LEFT)
                    currentMass.setVy(kineticEnergyY1)
                    a.setVy(0 - kineticEnergyY1)
                    b.setVy(0 - kineticEnergyY1)
                }
            }
        }
    }

    private fun checkForSpringCollisions(currentMass: Mass, springs: Set<Spring>, massesToRevert: MutableSet<Mass>) {
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
                    if (!collided) {
                        collided = true
                        firstContactPoint = currentMassX
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
                        sqrt((aVx * aVx + bVx * bVx + currentMass.oldVx * currentMass.oldVx) / 3.0 * ENERGY_LEFT)
                    val resultantVelocityY =
                        sqrt((aVy * aVy + bVy * bVy + currentMass.oldVy * currentMass.oldVy) / 3.0 * ENERGY_LEFT)
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
                    currentMassX > springMass1x + SPEED_LIMIT -> {
                        if (!collided) {
                            collided = true
                            firstContactPoint = currentMassX
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
                            sqrt((aVx * aVx + bVx * bVx + currentMass.oldVx * currentMass.oldVx) / 3.0 * ENERGY_LEFT)
                        currentMass.setVx(0 - kineticEnergyX1)
                        a.setVx(kineticEnergyX1)
                        b.setVx(kineticEnergyX1)
                    }
                }
            } else if (springMass1y == springMass2y) {
                if (currentMassY > springMass1y) {
                    //no collision, pruned
                } else if (currentMassY < springMass1y + SPEED_LIMIT) {
                    val a = spring.mass1
                    val b = spring.mass2
                    if (!collided) {
                        collided = true
                        firstContactPoint = currentMassX
                    }
                    massesToRevert.add(currentMass)
                    massesToRevert.add(a)
                    massesToRevert.add(b)
                    val aVy = a.oldVy
                    val bVy = b.oldVy
                    //lets [say] total (horizontal) kinetic energy is evenly distributed between 3 masses
                    val kineticEnergyY1 =
                        sqrt((aVy * aVy + bVy * bVy + currentMass.oldVy * currentMass.oldVy) / 3.0 * ENERGY_LEFT)
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

    companion object {
        private const val FRAME_DELAY = 20 //ms 30ms~33.33fps
        private const val GROUND_HEIGHT = 0.0
        private const val SURFACE_FRICTION = 0.1
        private const val SURFACE_REFLECTION = -0.75 // y velocity left when hitting ground
        private const val MODEL_REFLECTION = -0.75 // x velocity left when hitting ground
        private const val SPEED_LIMIT = 10.0 //solves model explosion problem!!
        private const val ENERGY_LEFT = 0.95

        @Volatile
        private var run = false
    }
}