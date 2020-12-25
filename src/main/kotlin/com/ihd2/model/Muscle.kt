package com.ihd2.model

class Muscle(id: Int) : Spring(id) {
    var amplitude = 0.0
    var phase = 0.0

    override fun toString(): String {
        return "$id a:$mass1 b:$mass2 amp:$amplitude phase:$phase restLeng:$restLength"
    }
}