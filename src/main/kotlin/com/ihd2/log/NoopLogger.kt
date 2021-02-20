package com.ihd2.log

class NoopLogger: Logger {

    override fun d(log: String) {
        // noop
    }
}