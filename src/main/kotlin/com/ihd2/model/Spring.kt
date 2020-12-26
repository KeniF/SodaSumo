package com.ihd2.model

open class Spring(val id: Int) {
    lateinit var mass1: Mass
    lateinit var mass2: Mass
    var restLength = 0.0

    override fun toString(): String {
        return "$id a:$mass1 b:$mass2 restlength:$restLength"
    }
}