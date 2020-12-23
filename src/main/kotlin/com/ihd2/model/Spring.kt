package com.ihd2.model

open class Spring {
    var name = 0
        protected set
    var mass1: Mass? = null
    var mass2: Mass? = null
    var restLength = 0.0

    constructor(name: Int) {
        this.name = name
    }

    override fun toString(): String {
        return "$name a:$mass1 b:$mass2 restlength:$restLength"
    }
}