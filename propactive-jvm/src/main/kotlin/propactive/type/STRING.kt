package propactive.type

import propactive.type.Type.Companion.INVALID
import propactive.type.Type.Companion.VALID

/** STRING type represents character strings, as defined by your JVM. */
object STRING : Type {
    override fun validate(value: Any) = value
        .runCatching { this as String; VALID }
        .getOrDefault(INVALID)
}