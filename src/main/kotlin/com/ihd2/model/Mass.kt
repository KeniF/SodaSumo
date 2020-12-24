package com.ihd2.model

class Mass(val id: Int) {
    var accX = 0.0

    var accY = 0.0

    var vx = 0.0
        set(vx) {
            oldVx = field
            field = vx
        }
    var vy = 0.0
        set(vy) {
            oldVy = field
            field = vy
        }
    var x = 0.0
        set(x) {
            oldX = field
            field = x
        }
    var y = 0.0
        set(y) {
            oldY = field
            field = y
        }

    var oldVx = 0.0
        private set
    var oldVy = 0.0
        private set
    var oldX = 0.0
        private set
    var oldY = 0.0
        private set

    fun revertX() {
        x = oldX
    }

    fun revertY() {
        y = oldY
    }

    override fun toString(): String {
        return "$id Vx:$vx Vy:$vy X:$x Y:$y"
    }
}