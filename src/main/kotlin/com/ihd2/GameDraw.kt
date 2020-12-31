package com.ihd2

import com.ihd2.dyn4j.NoSelfCollisionFilter
import com.ihd2.dyn4j.SimulationBody
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
import org.dyn4j.collision.Filter
import org.dyn4j.dynamics.BodyFixture
import org.dyn4j.geometry.Geometry
import org.dyn4j.geometry.MassType
import org.dyn4j.world.World
import java.lang.InterruptedException
import java.awt.RenderingHints
import java.awt.Font
import java.awt.geom.AffineTransform
import kotlin.math.*

class GameDraw : JComponent() {
    private var timeLimitMs = 15000L
    private val g2dEllipse = Ellipse2D.Double()
    private val g2dLine = Line2D.Double()

    private var physicsThread: Thread? = null
    private var gameFrames = 0
    private var model1: Model = Model()
    private var model2: Model = Model()
    private var firstContactPoint = 0.0
    private var collided = false
    private var resultMessage = ""
    private var world = World<SimulationBody>()

    init {
        world.settings.stepFrequency = 1.0 / FRAME_DELAY
    }

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

        drawDebugStats()
        drawVerticalLine()
        drawResult()

        val yFlip = AffineTransform.getScaleInstance(1.0, -1.0)
        gfx2d.transform(yFlip)
        val toMove = AffineTransform.getTranslateInstance(0.0, -height.toDouble())
        gfx2d.transform(toMove)

        for (body in world.bodies) {
            body.render(gfx2d, 1.0)
        }
//        drawModel(model1)
//        drawModel(model2)
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
                HEIGHT - (mass.getY() + MASS_SHIFT), MASS_DIAMETER, MASS_DIAMETER
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
                (mass1X + mass2X) / 2.0 - MUSCLE_MARKER_DIAMETER / 2.0,
                HEIGHT - ((mass1Y + mass2Y) / 2.0 + MUSCLE_MARKER_DIAMETER / 2.0),
                MUSCLE_MARKER_DIAMETER,
                MUSCLE_MARKER_DIAMETER
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

    fun provideModels(model1: Model, model2: Model) {
        world.removeAllBodies()
        addGroundToWorld()

        run = false
        this.model1 = model1
        this.model2 = model2
        val shiftRightM1 = Sodasumo.GAME_WIDTH / 2.0 - model1.boundingRectangle[3] - 10.0
        createModel(model1, shiftRightM1)
        val shiftRightM2 = Sodasumo.GAME_WIDTH / 2.0 - model2.boundingRectangle[2] + 10.0
        createModel(model2, shiftRightM2)
    }

    private fun createModel(model: Model, shiftRight: Double) {
        for (mass in model.massMap.values) {
            mass.setX(mass.getX() + shiftRight)
        }
        val filter = NoSelfCollisionFilter(model.name!!)
        for (spring in model.springMap.values) {
            createSprings(spring, filter)
        }
        for (spring in model.muscleMap.values) {
            createSprings(spring, filter)
        }
    }

    private fun createSprings(spring: Spring, filter: Filter) {
        world.addBody(createMassBody(spring.mass1, filter))
        world.addBody(createMassBody(spring.mass2, filter))

        val convex = Geometry.createRectangle(1.0, spring.currentLength)
        val bodyFixture = BodyFixture(convex)
        bodyFixture.filter = filter
        bodyFixture.density = 0.001
        val springBody = SimulationBody()
        springBody.addFixture(bodyFixture)
        springBody.rotate(spring.angle)
        springBody.translate(
            (spring.mass1.getX() + spring.mass2.getX()) / 2.0,
            (spring.mass1.getY() + spring.mass2.getY()) / 2.0)
        springBody.setMass(MassType.NORMAL)
        world.addBody(springBody)
    }

    private fun createMassBody(mass: Mass, filter: Filter): SimulationBody {
        val convex = Geometry.createCircle(MASS_DIAMETER / 2.0)
        val bodyFixture = BodyFixture(convex)
        bodyFixture.filter = filter
        bodyFixture.density = 1.0 / (MASS_DIAMETER / 2.0).pow(2) * PI
        val body = SimulationBody()
        body.addFixture(bodyFixture)
        body.translate(mass.getX(), mass.getY())
        body.setMass(MassType.NORMAL)
        return body
    }

    fun startGameLoop() {
        run = true
        gameFrames = 0
        collided = false
        resultMessage = ""

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

    private fun addGroundToWorld() {
        val ground = SimulationBody()
        val convex = Geometry.createRectangle(width.toDouble(), 1.0)
        val bf = BodyFixture(convex)
        ground.addFixture(bf)
        ground.translate(width / 2.0, GROUND_HEIGHT)
        ground.setMass(MassType.INFINITE)
        world.addBody(ground)
    }

    private fun animateAndStep() {
        animate()
        repaint()
        gameFrames++
        if (!invertM1) {
            model1.noOfFrames += 1
        } else {
            model1.noOfFrames -= 1
        }
        if (!invertM2) {
            model2.noOfFrames +=  1
        } else {
            model2.noOfFrames -= 1
        }
    }

    private fun endGame() {
        run = false
        setResultMessage(currentScore())
        repaint()
    }

    private fun setResultMessage(score: Int) {
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
    }

    private fun currentScore(): Int {
        return ((model1.boundRight - firstContactPoint +
                model2.boundLeft - firstContactPoint) / 20).toInt()
    }

    private fun animate() {
        accelerateSpringsAndMuscles(model1)
        //moveMasses(model1)
        accelerateSpringsAndMuscles(model2)
        //moveMasses(model2)
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
        private const val MASS_DIAMETER = 4.0
        private const val MUSCLE_MARKER_DIAMETER = 3.0
        private const val LINE_WIDTH = 0.4f
        private const val MASS_SHIFT = MASS_DIAMETER / 2.0 //Shift needed because specified point is ellipse's top-left
        private const val HEIGHT = 298.0 //need to invert height as top-left is (0,0)

        @Volatile
        private var run = false
    }
}