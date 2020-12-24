package com.ihd2.model

class Mass(val name: Int) {
    private var vx = 0.0
    private var vy = 0.0
    private var x = 0.0
    private var y = 0.0
    var oldVx = 0.0
        private set
    var oldVy = 0.0
        private set
    var oldX = 0.0
        private set
    var oldY = 0.0
        private set
    private var toRevertX = false
    private var toRevertY = false
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

    fun revertX() {
        x = oldX
    }

    fun revertY() {
        y = oldY
    }

    fun finallyRevert() {
        if (toRevertX) {
            x = oldX
            toRevertX = false
        }
        if (toRevertY) {
            y = oldY
            toRevertY = false
        }
    }

    override fun toString(): String {
        return "$name Vx:$vx Vy:$vy X:$x Y:$y"
    }
}