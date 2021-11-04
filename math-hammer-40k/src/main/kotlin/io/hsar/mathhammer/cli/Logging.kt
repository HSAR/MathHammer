package io.hsar.wh40k.combatsimulator.cli

import org.slf4j.Logger
import org.slf4j.LoggerFactory

interface Loggable {
    fun logger(): Logger {
        return LoggerFactory.getLogger(this.javaClass)
    }
}

