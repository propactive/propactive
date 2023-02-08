package io.github.propactive.logging

internal object Color {
    @JvmStatic internal fun blue(text: String) = "\u001b[0;34m$text\u001b[m"
    @JvmStatic internal fun magenta(text: String) = "\u001b[0;35m$text\u001b[m"
    @JvmStatic internal fun cyan(text: String) = "\u001b[0;36m$text\u001b[m"
}
