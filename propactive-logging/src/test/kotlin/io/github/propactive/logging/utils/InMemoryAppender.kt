package io.github.propactive.logging.utils

import java.io.Serializable
import java.util.UUID
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.core.Appender
import org.apache.logging.log4j.core.Filter
import org.apache.logging.log4j.core.Layout
import org.apache.logging.log4j.core.LogEvent
import org.apache.logging.log4j.core.appender.AbstractAppender
import org.apache.logging.log4j.core.config.Property

/**
 * An [Appender] that stores all the log events in memory.
 * This is useful for testing purposes.
 */
class InMemoryAppender(
    private val requiredLayout: Layout<out Serializable>,
    private val requiredLevel: Level,
) : AbstractAppender(
    InMemoryAppender::class.simpleName.plus("-${UUID.randomUUID()}"),
    NO_FILTER,
    requiredLayout,
    IGNORE_EXCEPTIONS,
    EMPTY_PROPERTIES,
) {
    private val events: MutableList<String> = ArrayList()

    internal fun events() = events.toList()

    override fun append(event: LogEvent) {
        this.requiredLayout
            .toByteArray(event.toImmutable())
            .let { msg -> String(msg).trim() + "\n" }
            .takeIf { event.level.isMoreSpecificThan(requiredLevel) }
            ?.let(events::add)
    }

    companion object {
        private val NO_FILTER: Filter? = null
        private const val IGNORE_EXCEPTIONS = false
        private val EMPTY_PROPERTIES = emptyArray<Property>()
    }
}
