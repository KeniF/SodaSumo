package com.ihd2.model

open class Spring(val id: Int) {
    var mass1: Mass? = null
    var mass2: Mass? = null
    var restLength = 0.0

    override fun toString(): String {
        return "$id a:$mass1 b:$mass2 restlength:$restLength"
    }
}