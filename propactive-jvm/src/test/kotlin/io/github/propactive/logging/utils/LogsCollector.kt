package io.github.propactive.logging.utils

import org.apache.logging.log4j.Level
import org.apache.logging.log4j.core.Appender
import org.apache.logging.log4j.core.Logger
import org.apache.logging.log4j.core.appender.ConsoleAppender

/**
 * A utility class that collects all the logs of a given logger.
 * This is useful for testing purposes.
 */
class LogsCollector(
    private val logger: Logger,
    private val requiredLevel: Level = Level.INFO
) {
    private val originalLevel = logger.level
    private val originalLayout = findConsoleAppender(logger).layout
    private lateinit var inMemoryAppender: InMemoryAppender

    internal fun start() {
        inMemoryAppender = InMemoryAppender(originalLayout, requiredLevel)
            .also(InMemoryAppender::start)
            .also(logger::addAppender)
            .apply { logger.level = requiredLevel }
    }

    internal fun stop() {
        inMemoryAppender
            .also(InMemoryAppender::stop)
            .also(logger::removeAppender)
            .apply { logger.level = originalLevel }
    }

    fun logs() = inMemoryAppender.events()

    fun find(predicate: (String) -> Boolean) = logs().find(predicate)

    private tailrec fun findConsoleAppender(logger: Logger): Appender {
        if (logger.appenders.isEmpty() && logger.isAdditive.not())
            error("Cannot find a ConsoleAppender for logger ${logger.name}")
        else return logger
            .appenders.values
            .find { it is ConsoleAppender }
            ?: findConsoleAppender(logger.parent)
    }
}
