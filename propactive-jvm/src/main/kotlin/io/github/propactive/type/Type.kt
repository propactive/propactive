package io.github.propactive.type

/**
 * Interface to create propactive types.
 * You can implement this to provide your own custom types.
 */
interface Type {
    companion object {
        const val VALID = true
        const val INVALID = false
    }
    fun validate(value: Any): Boolean
}
