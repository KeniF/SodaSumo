package com.ihd2.model

import kotlin.math.PI
import kotlin.math.atan
import kotlin.math.pow
import kotlin.math.sqrt

open class Spring(val id: Int) {
    lateinit var mass1: Mass
    lateinit var mass2: Mass
    var restLength = 0.0
    val currentLength: Double
        get() = sqrt((mass1.getX() - mass2.getX()).pow(2) + (mass1.getY() - mass2.getY()).pow(2))
    val angle: Double
        get() = atan((mass1.getX() - mass2.getX()) / (mass1.getY() - mass2.getY()))

    override fun toString(): String {
        return "$id a:$mass1 b:$mass2 restlength:$restLength"
    }
}