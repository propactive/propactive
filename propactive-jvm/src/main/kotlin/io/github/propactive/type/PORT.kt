package io.github.propactive.type

import io.github.propactive.type.Type.Companion.INVALID
import io.github.propactive.type.Type.Companion.VALID

/** Port number type is a 16-bit unsigned integer, as defined by [RFC 6335](https://datatracker.ietf.org/doc/html/rfc6335) */
object PORT : Type {
    override fun validate(value: Any) = value
        .toString()
        .toIntOrNull()
        ?.let { n -> if (n in 0..65535) VALID else INVALID }
        ?: INVALID
}
