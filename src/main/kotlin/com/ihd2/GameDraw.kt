package com.ihd2

import com.ihd2.dyn4j.NoSelfCollisionFilter
import com.ihd2.dyn4j.SimulationBody
import javax.swing.JComponent
import java.awt.geom.Line2D
import java.awt.Graphics2D
import kotlin.jvm.Volatile
import java.awt.Graphics
import java.awt.BasicStroke
import java.awt.Color
import com.ihd2.model.Mass
import com.ihd2.model.Model
import com.ihd2.model.Muscle
import com.ihd2.model.Spring
import org.dyn4j.collision.Filter
import org.dyn4j.dynamics.BodyFixture
import org.dyn4j.dynamics.joint.WheelJoint
import org.dyn4j.geometry.Geometry
import org.dyn4j.geometry.MassType
import org.dyn4j.geometry.Vector2
import org.dyn4j.world.World
import java.lang.InterruptedException
import java.awt.RenderingHints
import java.awt.Font
import java.awt.geom.AffineTransform
import kotlin.math.*

class GameDraw : JComponent() {
    private var timeLimitMs = 15000L
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
        world.settings.apply {
            maximumTranslation = SPEED_LIMIT
            stepFrequency = 1.0 / FRAME_DELAY
        }
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
        createModel(model1, shiftRightM1, 1)
        val shiftRightM2 = Sodasumo.GAME_WIDTH / 2.0 - model2.boundingRectangle[2] + 10.0
        createModel(model2, shiftRightM2, 2)
    }

    private fun createModel(model: Model, shiftRight: Double, id: Int) {
        val filter = NoSelfCollisionFilter(id)
        for (mass in model.massMap.values) {
            mass.setX(mass.getX() + shiftRight)
            world.addBody(createMassBodies(mass, filter))
        }
        for (spring in model.springMap.values) {
            createSpringBodies(spring, filter)
        }
        for (spring in model.muscleMap.values) {
            createSpringBodies(spring, filter)
        }
    }

    private fun createSpringBodies(spring: Spring, filter: Filter) {
        val isMuslce = spring is Muscle

        val convex = when (isMuslce) {
            true -> Geometry.createRectangle(1.0, spring.currentLength * 0.6)
            false -> Geometry.createRectangle(1.0, spring.currentLength)
        }
        val bodyFixture = BodyFixture(convex)
        bodyFixture.filter = filter
        bodyFixture.density = 0.001
        val springBody = SimulationBody()
        springBody.addFixture(bodyFixture)
        springBody.rotate(PI * 2.0 - spring.angle)
        springBody.translate(
            (spring.mass1.getX() + spring.mass2.getX()) / 2.0,
            (spring.mass1.getY() + spring.mass2.getY()) / 2.0)
        springBody.setMass(MassType.NORMAL)
        world.addBody(springBody)

        val linearAxis =  Vector2(
            spring.mass2.getX() - spring.mass1.getX(),
            spring.mass2.getY() - spring.mass1.getY())

        val upperLimit = when(isMuslce) {
            true -> 5.0
            false -> 0.0
        }
        val joint = WheelJoint(
            spring.mass1.simulationBody,
            springBody,
            Vector2(
                spring.mass1.getX(),
                spring.mass1.getY()),
            linearAxis).apply { setLimits(0.0, upperLimit) }
        val joint2 = WheelJoint(
            spring.mass2.simulationBody,
            springBody,
            Vector2(
                spring.mass2.getX(),
                spring.mass2.getY()),
            linearAxis).apply {
            setLimits(0.0, upperLimit)
        }
        world.addJoint(joint)
        world.addJoint(joint2)
    }

    private fun createMassBodies(mass: Mass, filter: Filter): SimulationBody {
        val convex = Geometry.createCircle(MASS_RADIUS)
        val bodyFixture = BodyFixture(convex)
        bodyFixture.filter = filter
        bodyFixture.density = 1.0 / (MASS_RADIUS.pow(2) * PI)
        val body = SimulationBody()
        body.addFixture(bodyFixture)
        body.translate(mass.getX(), mass.getY())
        body.setMass(MassType.NORMAL)
        mass.simulationBody = body
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
        val convex = Geometry.createRectangle(width.toDouble(), 30.0)
        val bf = BodyFixture(convex)
        ground.addFixture(bf)
        ground.translate(width / 2.0, GROUND_HEIGHT - 10.0)
        ground.setMass(MassType.INFINITE)
        world.addBody(ground)
    }

    private fun animateAndStep() {
        calculateForces()
        world.update(FRAME_DELAY.toDouble())
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

    private fun calculateForces() {
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
                        mass1.applyForce(
                                -(resultantAcceleration * cosineAngle),
                                resultantAcceleration * sineAngle)
                        mass2.applyForce(
                                resultantAcceleration * cosineAngle,
                                -(resultantAcceleration * sineAngle))
                    }
                    mass2Y < mass1Y -> {
                        mass1.applyForce(
                            -(resultantAcceleration * cosineAngle),
                            -(resultantAcceleration * sineAngle)
                        )
                        mass2.applyForce(
                            resultantAcceleration * cosineAngle,
                            resultantAcceleration * sineAngle
                        )
                    }
                    else -> {
                        mass1.applyForce(-resultantAcceleration, 0.0)
                        mass2.applyForce(resultantAcceleration, 0.0)
                    }
                }
            } else if (mass1X < mass2X) {
                when {
                    mass2Y > mass1Y -> {
                        mass1.applyForce(
                            resultantAcceleration * cosineAngle,
                            resultantAcceleration * sineAngle
                        )
                        mass2.applyForce(
                            -(resultantAcceleration * cosineAngle),
                            -(resultantAcceleration * sineAngle)
                        )
                    }
                    mass2Y < mass1Y -> {
                        mass1.applyForce(
                            resultantAcceleration * cosineAngle,
                            -(resultantAcceleration * sineAngle)
                        )
                        mass2.applyForce(
                            -(resultantAcceleration * cosineAngle),
                            resultantAcceleration * sineAngle
                        )
                    }
                    else -> {
                        mass1.applyForce(resultantAcceleration, 0.0) //x
                        mass2.applyForce(-resultantAcceleration, 0.0) //x
                    }
                }
            } else {
                if (mass1Y > mass2Y) {
                    mass1.applyForce(0.0, -resultantAcceleration)
                    mass2.applyForce(0.0, resultantAcceleration)
                } else if (mass1Y < mass2Y) {
                    mass1.applyForce(0.0, resultantAcceleration)
                    mass2.applyForce(0.0, -resultantAcceleration)
                }
            }
        }
    }


    companion object {
        private const val FRAME_DELAY = 15 //ms 30ms~33.33fps
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
        private const val MASS_RADIUS = 2.0
        private const val MUSCLE_MARKER_DIAMETER = 3.0
        private const val LINE_WIDTH = 0.4f
        private const val HEIGHT = 298.0 //need to invert height as top-left is (0,0)

        @Volatile
        private var run = false
    }
}