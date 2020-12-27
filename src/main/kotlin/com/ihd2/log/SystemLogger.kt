package com.ihd2.log

class SystemLogger : Logger {
    override fun d(log: String) {
        println(log)
    }
}