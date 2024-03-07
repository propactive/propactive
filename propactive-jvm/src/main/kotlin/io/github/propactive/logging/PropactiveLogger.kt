package io.github.propactive.logging

import org.apache.logging.log4j.kotlin.logger

internal object PropactiveLogger {
    internal const val NAME = "io.github.propactive"
    private val DELEGATE = logger(NAME)

    @JvmStatic
    internal fun <T : Any> T.info(supplier: T.() -> String): T = this.apply {
        DELEGATE.info { Color.magenta(supplier.invoke(this)) }
    }

    @JvmStatic
    internal fun <T : Any> T.debug(supplier: T.() -> String): T = this.apply {
        DELEGATE.debug { Color.blue(supplier.invoke(this)) }
    }

    @JvmStatic
    internal fun <T : Any> T.trace(supplier: T.() -> String): T = this.apply {
        DELEGATE.trace { Color.cyan(supplier.invoke(this)) }
    }
}
