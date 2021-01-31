package com.ihd2

import com.ihd2.graphics.GraphicsRenderer
import com.ihd2.graphics.JavaAwtRenderer
import javax.swing.JComponent
import java.awt.Graphics2D
import kotlin.jvm.Volatile
import java.awt.Graphics
import java.awt.Color
import com.ihd2.model.Model
import com.ihd2.model.Scene
import com.ihd2.physics.PhysicalWorld
import com.ihd2.physics.PhysicsConfig
import java.lang.InterruptedException
import kotlin.math.*

class GameDraw: JComponent() {
    private var timeLimitMs = 15000L

    private var physicsThread: Thread? = null
    private var model1: Model = Model()
    private var model2: Model = Model()
    private var resultMessage = ""
    private val renderer = JavaAwtRenderer(DEBUG)
    private val physicalWorld = PhysicalWorld()

    @Volatile
    var paused = false

    @Volatile
    private var run = false

    fun init(model1: Model, model2: Model) {
        run = true
        resultMessage = ""

        this.model1 = model1
        this.model2 = model2

        val scene = Scene(model1, model2)
        physicalWorld.reset(scene, PhysicsConfig.CLASSIC, width)
    }

    fun invertM1() {
        model1.flipModel = !model1.flipModel
    }

    fun invertM2() {
        model2.flipModel = !model2.flipModel
    }

    fun stepAnimationManually() {
        if (!run) return
        paused = true
        repaintWorld()
    }

    override fun paint(g: Graphics) {
        renderer.initFrame(g as Graphics2D, height - 2, width)
        physicalWorld.render(renderer)

        drawCollisionLine(renderer)
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

    fun startDraw() {
        physicsThread = Thread {
            val curThread = Thread.currentThread()
            var beforeRun = System.currentTimeMillis()
            while (curThread === physicsThread && run) {
                if (physicalWorld.gameFrames > timeLimitMs / FRAME_DELAY) {
                    endGame()
                } else if (!paused) {
                    repaintWorld()
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

    private fun drawCollisionLine(renderer: GraphicsRenderer) {
        physicalWorld.firstCollisionInfo.apply {
            renderer.drawLine(
                Color.GRAY,
                collisionPoint,
                0.0,
                collisionPoint,
                1000.0)
        }
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
            "Frames: ${physicalWorld.gameFrames} Frames_m1: ${model1.noOfFrames} Frames_m2: ${model2.noOfFrames}")
    }


    private fun repaintWorld() {
        physicalWorld.generateNextFrame()
        repaint()
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
        val collisionPoint = physicalWorld.firstCollisionInfo.collisionPoint
        return ((model1.boundRight - collisionPoint +
                model2.boundLeft - collisionPoint) / 20).toInt()
    }

    companion object {
        private const val FRAME_DELAY = 20 //ms 30ms~33.33fps
    }
}