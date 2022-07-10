package propactive.type

import propactive.type.Type.Companion.INVALID
import propactive.type.Type.Companion.VALID

/** INTEGER type is a 32-bit signed integer, as defined by your JVM. */
object INTEGER : Type {
    override fun validate(value: Any) = value
        .runCatching { toString().toInt(); VALID }
        .getOrDefault(INVALID)
}