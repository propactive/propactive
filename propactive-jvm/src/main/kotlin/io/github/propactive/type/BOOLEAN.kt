package io.github.propactive.type

import io.github.propactive.type.Type.Companion.INVALID
import io.github.propactive.type.Type.Companion.VALID

/** BOOLEAN type as defined by your JVM. */
object BOOLEAN : Type {
    override fun validate(value: Any) = when (value.toString().lowercase()) {
        "true" -> VALID
        "false" -> VALID
        else -> INVALID
    }
}
