package io.github.propactive.logging

import org.apache.logging.log4j.kotlin.logger

object PropactiveLogger {
    internal const val NAME = "Propactive"
    private val DELEGATE = logger(NAME)

    @JvmStatic
    fun <T : Any> T.info(supplier: T.() -> String): T = this.apply {
        DELEGATE.info { Color.magenta(supplier.invoke(this)) }
    }

    @JvmStatic
    fun <T : Any> T.debug(supplier: T.() -> String): T = this.apply {
        DELEGATE.debug { Color.blue(supplier.invoke(this)) }
    }

    @JvmStatic
    fun <T : Any> T.trace(supplier: T.() -> String): T = this.apply {
        DELEGATE.trace { Color.cyan(supplier.invoke(this)) }
    }
}