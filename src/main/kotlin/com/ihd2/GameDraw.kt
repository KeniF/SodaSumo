package com.ihd2

import javax.swing.JComponent
import java.awt.geom.Line2D
import java.awt.geom.Ellipse2D
import java.awt.Graphics2D
import kotlin.jvm.Volatile
import java.awt.Graphics
import java.awt.BasicStroke
import java.awt.Color
import com.ihd2.model.Mass
import com.ihd2.model.Model
import com.ihd2.model.Spring
import java.lang.InterruptedException
import java.awt.RenderingHints
import java.awt.Font
import kotlin.math.*

class GameDraw : JComponent() {
    private var timeLimitMs = 15000L
    private val horizontalLine = Line2D.Double()
    private val lineOld = Line2D.Double()
    private val lineNew = Line2D.Double()
    private val massLine = Line2D.Double()
    private val g2dEllipse = Ellipse2D.Double()
    private val g2dLine = Line2D.Double()

    private var physicsThread: Thread? = null
    private var gameFrames = 0
    private var model1: Model = Model()
    private var model2: Model = Model()
    private var firstContactPoint = 0.0
    private var collided = false
    private var resultMessage = ""

    @Volatile
    private var invertM1 = false
    @Volatile
    private var invertM2 = false
    @Volatile
    var paused = false

    private lateinit var gfx2d: Graphics2D

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
        gfx2d = g as Graphics2D
        gfx2d.stroke = BasicStroke(LINE_WIDTH)
        gfx2d.setRenderingHints(renderingHints) //turns on anti-aliasing
        drawModel(model1)
        drawModel(model2)
        drawVerticalLine()
        drawResult()
        drawDebugStats()
    }

    fun stop() {
        run = false
        paused = false
    }

    fun setTimeLimit(milliseconds: Long) {
        timeLimitMs = milliseconds
    }

    private fun drawVerticalLine() {
        if (!collided) return

        gfx2d.color = Color.GRAY
        g2dLine.setLine(firstContactPoint, HEIGHT + 10.0, firstContactPoint, HEIGHT - 1000.0)
        gfx2d.draw(g2dLine)
    }

    private fun drawResult() {
        if (resultMessage == "") return

        gfx2d.color = Color.BLUE.darker().darker()
        gfx2d.font = resultFont
        gfx2d.drawString(resultMessage, Sodasumo.GAME_WIDTH.toInt() / 2 - 150, 50)
    }

    private fun drawModel(model: Model) {
        drawMasses(model)
        drawSprings(model)
        drawMuscles(model)
    }

    private fun drawMasses(model: Model) {
        for (mass in model.massMap.values) {
            gfx2d.color = when(DEBUG) {
                true -> Color.GRAY
                false -> Color.BLACK
            }
            g2dEllipse.setFrame(
                mass.getX() - MASS_SHIFT,
                HEIGHT - (mass.getY() + MASS_SHIFT), MASS_SIZE, MASS_SIZE
            )
            gfx2d.fill(g2dEllipse)
            if (DEBUG) {
                gfx2d.color = Color.BLACK
                gfx2d.font = debugFont
                gfx2d.drawString("${mass.id}", mass.getX().toInt(), (HEIGHT - mass.getY()).toInt())
            }
        }
    }

    private fun drawSprings(model: Model) {
        for (spring in model.springMap.values) {
            val mass1 = spring.mass1
            val mass2 = spring.mass2

            gfx2d.color = Color.BLACK
            g2dLine.setLine(
                mass1.getX(), HEIGHT - mass1.getY(),
                mass2.getX(), HEIGHT - mass2.getY()
            )
            gfx2d.draw(g2dLine)
        }
    }

    private fun drawMuscles(model: Model) {
        for (muscle in model.muscleMap.values) {
            val mass1 = muscle.mass1
            val mass2 = muscle.mass2

            val mass1X = mass1.getX()
            val mass1Y = mass1.getY()
            val mass2X = mass2.getX()
            val mass2Y = mass2.getY()
            gfx2d.color = Color.BLACK
            g2dLine.setLine(
                mass1X, HEIGHT - mass1Y,
                mass2X, HEIGHT - mass2Y
            )
            gfx2d.draw(g2dLine)

            g2dEllipse.setFrame(
                (mass1X + mass2X) / 2.0 - MUSCLE_MARKER_SIZE / 2.0,
                HEIGHT - ((mass1Y + mass2Y) / 2.0 + MUSCLE_MARKER_SIZE / 2.0),
                MUSCLE_MARKER_SIZE,
                MUSCLE_MARKER_SIZE
            )
            gfx2d.fill(g2dEllipse)
        }
    }

    private fun drawDebugStats() {
        if (DEBUG) {
            gfx2d.color = Color.GRAY
            gfx2d.font = debugFont
            val string =
                "Frames: $gameFrames Frames_m1: ${model1.noOfFrames} Frames_m2: ${model2.noOfFrames}"
            gfx2d.drawString(string, 2, 10)
        }
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
        val shiftRight = Sodasumo.GAME_WIDTH / 2.0 - br[3] - 10.0
        for (i in 1..model1.massMap.size) {
            model1.getMass(i)!!.setX(model1.getMass(i)!!.getX() + shiftRight)
        }
    }

    fun provideModel2(model: Model) {
        run = false
        model2 = model
        val br = model2.boundingRectangle
        val shiftRight = Sodasumo.GAME_WIDTH / 2.0 - br[2] + 10.0
        for (i in 1..model2.massMap.size) {
            model2.getMass(i)!!.setX(model2.getMass(i)!!.getX() + shiftRight)
        }
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
        if (!invertM1)
            model1.noOfFrames = model1.noOfFrames + 1
        else model1.noOfFrames =
            model1.noOfFrames - 1
        if (!invertM2)
            model2.noOfFrames = model2.noOfFrames + 1
        else model2.noOfFrames = model2.noOfFrames - 1
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
        val totalNo: Int = if (isMuscle) model.muscleMap.size else model.springMap.size
        for (i in 1..totalNo) {
            var restLength: Double
            var mass1: Mass
            var mass2: Mass

            if (isMuscle) {
                val muscle = model.getMuscle(i)!!

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
                val spring = model.getSpring(i)!!
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
        for (i in 1..model.massMap.size) {
            //damping for F=-fv
            val mass = model.getMass(i)!!
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

        for (j in 1..model1.massMap.size) {
            val currentMass = model1.getMass(j)!!
            checkForSpringCollisions(currentMass, model2.springMap, massesToRevert)
            checkForSpringCollisions(currentMass, model2.muscleMap, massesToRevert)
        }

        var currentMass1X: Double
        var currentMass2X: Double
        var currentMass1Y: Double
        var currentMass2Y: Double
        var kineticEnergyX1: Double
        var kineticEnergyY1: Double
        var slopeOfLine: Double
        var yIntercept: Double
        var currentMassX: Double
        var currentMassY: Double
        var currentMassOldX: Double
        var currentMassOldY: Double
        var currentMassVx: Double
        var currentMassVy: Double
        var resultOld: Int
        var resultNew: Int
        var currentMass: Mass
        var cmass1: Mass?
        var cmass2: Mass?
        for (j in 1..model2.massMap.size) {
            currentMass = model2.getMass(j)!!
            currentMassX = currentMass.getX()
            currentMassY = currentMass.getY()
            for (i in 1..model1.springMap.size) {
                cmass1 = model1.getSpring(i)!!.mass1
                cmass2 = model1.getSpring(i)!!.mass2
                currentMass1X = cmass1.getX()
                currentMass2X = cmass2.getX()
                currentMass1Y = cmass1.getY()
                currentMass2Y = cmass2.getY()
                if (currentMassX > currentMass1X && currentMassX > currentMass2X) { //not collided
                    //prune
                } else if (currentMass1X != currentMass2X && currentMass1Y != currentMass2Y) { // not vertical / horizontal
                    slopeOfLine = (currentMass1Y - currentMass2Y) / (currentMass1X - currentMass2X)
                    currentMassOldX = currentMass.oldX
                    currentMassOldY = currentMass.oldY
                    //y=mx+c
                    yIntercept = currentMass1Y - slopeOfLine * currentMass1X
                    resultNew = throwPointInLine(currentMassX, currentMassY, yIntercept, slopeOfLine)
                    val mass1OldX = cmass1.oldX
                    val mass2OldX = cmass2.oldX
                    val mass1OldY = cmass1.oldY
                    val mass2OldY = cmass2.oldY
                    slopeOfLine = (mass1OldY - mass2OldY) / (mass1OldX - mass2OldX)
                    yIntercept = mass1OldY - slopeOfLine * mass1OldX
                    resultOld = throwPointInLine(currentMassOldX, currentMassOldY, yIntercept, slopeOfLine)
                    horizontalLine.setLine(currentMassX, currentMassY, 10000.0, currentMassY)
                    lineNew.setLine(currentMass1X, currentMass1Y, currentMass2X, currentMass2Y)
                    lineOld.setLine(mass1OldX, mass1OldY, mass2OldX, mass2OldY)
                    massLine.setLine(currentMassX, currentMassY, currentMassOldX, currentMassOldY)
                    var countIntersections = 0
                    if (horizontalLine.intersectsLine(lineNew)) countIntersections++
                    if (horizontalLine.intersectsLine(lineOld)) countIntersections++
                    if (lineNew.intersectsLine(massLine) || lineOld.intersectsLine(massLine) || resultOld == -1 && resultNew == 1 && countIntersections == 1) {
                        val a = model1.getSpring(i)!!.mass1
                        val b = model1.getSpring(i)!!.mass2
                        if (!collided) {
                            collided = true
                            firstContactPoint = currentMassX
                        }
                        massesToRevert.add(currentMass)
                        massesToRevert.add(a)
                        massesToRevert.add(b)
                        currentMassVx = currentMass.oldVx
                        currentMassVy = currentMass.oldVy
                        val aVx = a.oldVx
                        val bVx = b.oldVx
                        val aVy = a.oldVy
                        val bVy = b.oldVy
                        //lets [say] total (horizontal) kinetic energy is evenly distributed between 3 masses
                        kineticEnergyX1 =
                            sqrt((aVx * aVx + bVx * bVx + currentMassVx * currentMassVx) / 3.0 * ENERGY_LEFT)
                        kineticEnergyY1 =
                            sqrt((aVy * aVy + bVy * bVy + currentMassVy * currentMassVy) / 3.0 * ENERGY_LEFT)
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
                } else if (currentMass1X == currentMass2X) {
                    when {
                        currentMassX > currentMass1X -> {
                        }
                        currentMassX > currentMass1X - SPEED_LIMIT -> {
                            val a = model1.getSpring(i)!!.mass1
                            val b = model1.getSpring(i)!!.mass2
                            if (!collided) {
                                collided = true
                                firstContactPoint = currentMassX
                            }
                            massesToRevert.add(currentMass)
                            massesToRevert.add(a)
                            massesToRevert.add(b)
                            currentMassVx = currentMass.oldVx
                            val aVx = a.oldVx
                            val bVx = b.oldVx
                            //lets [say] total (horizontal) kinetic energy is evenly distributed between 3 masses
                            kineticEnergyX1 =
                                sqrt((aVx * aVx + bVx * bVx + currentMassVx * currentMassVx) / 3.0 * ENERGY_LEFT)
                            currentMass.setVx(kineticEnergyX1)
                            a.setVx(0 - kineticEnergyX1)
                            b.setVx(0 - kineticEnergyX1)
                        }
                    }
                } else if (currentMass1Y == currentMass2Y) {
                    if (currentMassY > currentMass1Y) {
                        //no collision, pruned
                    } else if (currentMassY < currentMass1Y - SPEED_LIMIT) {
                        val a = model1.getSpring(i)!!.mass1
                        val b = model1.getSpring(i)!!.mass2
                        if (!collided) {
                            collided = true
                            firstContactPoint = currentMassX
                        }
                        massesToRevert.add(currentMass)
                        massesToRevert.add(a)
                        massesToRevert.add(b)
                        currentMassVy = currentMass.oldVy
                        val aVy = a.oldVy
                        val bVy = b.oldVy
                        //lets [say] total (horizontal) kinetic energy is evenly distributed between 3 masses
                        kineticEnergyY1 =
                            sqrt((aVy * aVy + bVy * bVy + currentMassVy * currentMassVy) / 3.0 * ENERGY_LEFT)
                        currentMass.setVy(kineticEnergyY1)
                        a.setVy(0 - kineticEnergyY1)
                        b.setVy(0 - kineticEnergyY1)
                    }
                }
            }
            for (i in 1..model1.muscleMap.size) {
                cmass1 = model1.getMuscle(i)!!.mass1
                cmass2 = model1.getMuscle(i)!!.mass2
                currentMass1X = cmass1.getX()
                currentMass2X = cmass2.getX()
                currentMass1Y = cmass1.getY()
                currentMass2Y = cmass2.getY()
                if (currentMassX > currentMass1X && currentMassX > currentMass2X) {
                    //prune
                } else if (currentMass1X != currentMass2X && currentMass1Y != currentMass2Y) { // not vertical / horizontal
                    slopeOfLine = (currentMass1Y - currentMass2Y) / (currentMass1X - currentMass2X)
                    currentMassOldX = currentMass.oldX
                    currentMassOldY = currentMass.oldY
                    //y=mx+c
                    yIntercept = currentMass1Y - slopeOfLine * currentMass1X
                    resultNew = throwPointInLine(currentMassX, currentMassY, yIntercept, slopeOfLine)
                    val mass1OldX = cmass1.oldX
                    val mass2OldX = cmass2.oldX
                    val mass1OldY = cmass1.oldY
                    val mass2OldY = cmass2.oldY
                    slopeOfLine = (mass1OldY - mass2OldY) / (mass1OldX - mass2OldX)
                    yIntercept = mass1OldY - slopeOfLine * mass1OldX
                    resultOld = throwPointInLine(currentMassOldX, currentMassOldY, yIntercept, slopeOfLine)
                    horizontalLine.setLine(currentMassX, currentMassY, 10000.0, currentMassY)
                    lineNew.setLine(currentMass1X, currentMass1Y, currentMass2X, currentMass2Y)
                    lineOld.setLine(mass1OldX, mass1OldY, mass2OldX, mass2OldY)
                    massLine.setLine(currentMassX, currentMassY, currentMassOldX, currentMassOldY)
                    var countIntersections = 0
                    if (horizontalLine.intersectsLine(lineNew)) countIntersections++
                    if (horizontalLine.intersectsLine(lineOld)) countIntersections++
                    if (lineNew.intersectsLine(massLine) ||
                        lineOld.intersectsLine(massLine) ||
                        resultOld == -1 && resultNew == 1 && countIntersections == 1) {
                        val a = model1.getMuscle(i)!!.mass1
                        val b = model1.getMuscle(i)!!.mass2
                        if (!collided) {
                            collided = true
                            firstContactPoint = currentMassX
                        }
                        massesToRevert.add(currentMass)
                        massesToRevert.add(a)
                        massesToRevert.add(b)
                        currentMassVx = currentMass.oldVx
                        currentMassVy = currentMass.oldVy
                        val aVx = a.oldVx
                        val bVx = b.oldVx
                        val aVy = a.oldVy
                        val bVy = b.oldVy
                        //lets [say] total (horizontal) kinetic energy is evenly distributed between 3 masses
                        kineticEnergyX1 =
                            sqrt((aVx * aVx + bVx * bVx + currentMassVx * currentMassVx) / 3.0 * ENERGY_LEFT)
                        kineticEnergyY1 =
                            sqrt((aVy * aVy + bVy * bVy + currentMassVy * currentMassVy) / 3.0 * ENERGY_LEFT)
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
                        } else {
                            if (resultOld == -1) {
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
                } else if (currentMass1X == currentMass2X) {
                    when {
                        currentMassX > currentMass1X -> {
                        }
                        currentMassX > currentMass1X - SPEED_LIMIT -> {
                            val a = model1.getMuscle(i)!!.mass1
                            val b = model1.getMuscle(i)!!.mass2
                            if (!collided) {
                                collided = true
                                firstContactPoint = currentMassX
                            }
                            massesToRevert.add(currentMass)
                            massesToRevert.add(a)
                            massesToRevert.add(b)
                            currentMassVx = currentMass.oldVx
                            val aVx = a.oldVx
                            val bVx = b.oldVx
                            //lets [say] total (horizontal) kinetic energy is evenly distributed between 3 masses
                            kineticEnergyX1 =
                                sqrt((aVx * aVx + bVx * bVx + currentMassVx * currentMassVx) / 3.0 * ENERGY_LEFT)
                            currentMass.setVx(kineticEnergyX1)
                            a.setVx(0 - kineticEnergyX1)
                            b.setVx(0 - kineticEnergyX1)
                        }
                    }
                } else if (currentMass1Y == currentMass2Y) {
                    if (currentMassY > currentMass1Y) {
                        //no collision, pruned
                    } else if (currentMassY < currentMass1Y - SPEED_LIMIT) {
                        val a = model1.getMuscle(i)!!.mass1
                        val b = model1.getMuscle(i)!!.mass2
                        if (!collided) {
                            collided = true
                            firstContactPoint = currentMassX
                        }
                        massesToRevert.add(currentMass)
                        massesToRevert.add(a)
                        massesToRevert.add(b)
                        currentMassVy = currentMass.oldVy
                        val aVy = a.oldVy
                        val bVy = b.oldVy
                        //lets [say] total (horizontal) kinetic energy is evenly distributed between 3 masses
                        kineticEnergyY1 =
                            sqrt((aVy * aVy + bVy * bVy + currentMassVy * currentMassVy) / 3.0 * ENERGY_LEFT)
                        currentMass.setVy(kineticEnergyY1)
                        a.setVy(0 - kineticEnergyY1)
                        b.setVy(0 - kineticEnergyY1)
                    }
                }
            }
        }
        for (mass in massesToRevert) {
            mass.revertPoints()
        }
    }

    private fun checkForSpringCollisions(currentMass: Mass, springMap: Map<Int, Spring>, massesToRevert: MutableSet<Mass>) {
        val currentMassX = currentMass.getX()
        val currentMassY = currentMass.getY()
        for (spring: Spring in springMap.values) {
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
                val resultNew = throwPointInLine(currentMassX, currentMassY, yInterceptNew, slopeOfLine)
                val mass1OldX = springMass1.oldX
                val mass2OldX = springMass2.oldX
                val mass1OldY = springMass1.oldY
                val mass2OldY = springMass2.oldY
                slopeOfLine = (mass1OldY - mass2OldY) / (mass1OldX - mass2OldX)
                val yInterceptOld = mass1OldY - slopeOfLine * mass1OldX
                val resultOld = throwPointInLine(currentMassOldX, currentMassOldY, yInterceptOld, slopeOfLine)
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

    private fun throwPointInLine(x: Double, y: Double, yInter: Double, slope: Double): Int {
        //y-mx-c, returns 1 if on the left
        var feedback = 1
        val result = y - slope * x - yInter
        if (slope > 0) {
            if (result < 0) feedback = -1 else if (result == 0.0) feedback = 0
        } else if (slope < 0) {
            if (result > 0) feedback = -1 else if (result == 0.0) feedback = 0
        }
        return feedback
    }

    companion object {
        private const val FRAME_DELAY = 20 //ms 30ms~33.33fps
        private const val GROUND_HEIGHT = 0.0
        private const val SURFACE_FRICTION = 0.1
        private const val SURFACE_REFLECTION = -0.75 // y velocity left when hitting ground
        private const val MODEL_REFLECTION = -0.75 // x velocity left when hitting ground
        private const val SPEED_LIMIT = 10.0 //solves model explosion problem!!
        private const val ENERGY_LEFT = 0.95
        private val renderingHints = RenderingHints(
            RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON
        )
        private val debugFont = Font("Arial", Font.PLAIN, 9)
        private val resultFont = Font("Arial", Font.PLAIN, 20)
        private const val MASS_SIZE = 4.0
        private const val MUSCLE_MARKER_SIZE = 3.0
        private const val LINE_WIDTH = 0.4f
        private const val MASS_SHIFT = MASS_SIZE / 2.0 //Shift needed because specified point is ellipse's top-left
        private const val HEIGHT = 298.0 //need to invert height as top-left is (0,0)

        @Volatile
        private var run = false
    }
}