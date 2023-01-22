package io.github.propactive.logging

import io.github.propactive.logging.PropactiveLogger.info
import io.github.propactive.logging.PropactiveLogger.debug
import io.github.propactive.logging.PropactiveLogger.trace
import io.github.propactive.logging.utils.LogsCollector
import io.github.propactive.logging.utils.LogsCollectorExtension
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.string.shouldStartWith
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.core.Logger
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(LogsCollectorExtension::class)
class LoggerTest {
    private val infoCollector = LogsCollector(LogManager.getLogger(PropactiveLogger.NAME) as Logger, Level.INFO)
    private val debugCollector = LogsCollector(LogManager.getLogger(PropactiveLogger.NAME) as Logger, Level.DEBUG)
    private val traceCollector = LogsCollector(LogManager.getLogger(PropactiveLogger.NAME) as Logger, Level.TRACE)

    @Test
    fun shouldLogInfoLogsToCorrectLevel() {
        info {
            "..."
        }

        infoCollector.logs() shouldHaveSize 1
        debugCollector.logs() shouldHaveSize 1
        traceCollector.logs() shouldHaveSize 1
    }

    @Test
    fun shouldLogDebugLogsToCorrectLevel() {
        debug {
            "..."
        }

        infoCollector.logs() shouldHaveSize 0
        debugCollector.logs() shouldHaveSize 1
        traceCollector.logs() shouldHaveSize 1
    }

    @Test
    fun shouldLogTraceLogsToCorrectLevel() {
        trace {
            "..."
        }

        infoCollector.logs() shouldHaveSize 0
        debugCollector.logs() shouldHaveSize 0
        traceCollector.logs() shouldHaveSize 1
    }

    @Test
    fun shouldPreserveOriginalInfoMessage() {
        info {
            "A Logged Info Message"
        }

        infoCollector
            .find { it.contains("A Logged Info Message") }
            .shouldNotBeNull()
    }

    @Test
    fun shouldPreserveOriginalDebugMessage() {
        debug {
            "A Logged Debug Message"
        }

        debugCollector
            .find { it.contains("A Logged Debug Message") }
            .shouldNotBeNull()
    }

    @Test
    fun shouldPreserveOriginalTraceMessage() {
        trace {
            "A Logged Trace Message"
        }

        traceCollector
            .find { it.contains("A Logged Trace Message") }
            .shouldNotBeNull()
    }

    @Test
    fun shouldHighlightInfoWithMagentaColor() {
        info {
            "..."
        }

        infoCollector
            .logs().single()
            .shouldStartWith(Color.magenta("..."))
    }

    @Test
    fun shouldHighlightDebugWithBlueColor() {
        debug {
            "..."
        }

        debugCollector
            .logs().single()
            .shouldStartWith(Color.blue("..."))
    }

    @Test
    fun shouldHighlightTraceWithCyanColor() {
        trace {
            "..."
        }

        traceCollector
            .logs().single()
            .shouldStartWith(Color.cyan("..."))
    }
}
