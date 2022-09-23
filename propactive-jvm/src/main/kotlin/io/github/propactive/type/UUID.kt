package io.github.propactive.type

import io.github.propactive.type.Type.Companion.INVALID
import io.github.propactive.type.Type.Companion.VALID

/** UUID type as defined by [RFC 4122](https://www.ietf.org/rfc/rfc4122.txt) */
object UUID : Type {
    override fun validate(value: Any) = value
        .runCatching { java.util.UUID.fromString(toString()); VALID }
        .getOrDefault(INVALID)
}