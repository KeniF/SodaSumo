package com.ihd2.model

class Mass(val id: Int) {
    var ax = 0.0
        private set
    var ay = 0.0
        private set
    var oldVx = 0.0
        private set
    var oldVy = 0.0
        private set
    var oldX = 0.0
        private set
    var oldY = 0.0
        private set
    private var vx = 0.0
    private var vy = 0.0
    private var x = 0.0
    private var y = 0.0

    fun getX(): Double {
        return x
    }

    fun setX(x: Double) {
        oldX = this.x
        this.x = x
    }

    fun getY(): Double {
        return y
    }

    fun setY(y: Double) {
        oldY = this.y
        this.y = y
    }

    fun getVx(): Double {
        return vx
    }

    fun setVx(vx: Double) {
        oldVx = this.vx
        this.vx = vx
    }

    fun getVy(): Double {
        return vy
    }

    fun setVy(vy: Double) {
        oldVy = this.vy
        this.vy = vy
    }

    fun revertPoints() {
        x = oldX
        y = oldY
    }

    fun accelerate(x: Double, y: Double) {
        ax += x
        ay += y
    }

    fun clearAccelerations() {
        ax = 0.0
        ay = 0.0
    }

    override fun toString(): String {
        return "$id Vx:$vx Vy:$vy X:$x Y:$y"
    }
}