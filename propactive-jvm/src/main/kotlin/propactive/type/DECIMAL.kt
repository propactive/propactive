package propactive.type

import propactive.type.Type.Companion.INVALID
import propactive.type.Type.Companion.VALID

/** DECIMAL type as defined by [IEEE 754](https://standards.ieee.org/ieee/754/6210/) */
object DECIMAL : Type {
    override fun validate(value: Any) = value
        .runCatching { toString().toDouble(); VALID }
        .getOrDefault(INVALID)
}