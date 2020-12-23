package com.ihd2.model

class Muscle(name: Int) : Spring(name) {
    var amplitude = 0.0
    var phase = 0.0

    override fun toString(): String {
        return "$name a:$mass1 b:$mass2 amp:$amplitude phase:$phase restLeng:$restLength"
    }

    init {
        super.name = name
    }
}