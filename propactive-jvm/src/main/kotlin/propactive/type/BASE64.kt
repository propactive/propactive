package propactive.type

import propactive.type.Type.Companion.INVALID
import propactive.type.Type.Companion.VALID
import java.util.Base64.getDecoder

/** BASE64 type as defined by [RFC 4648](https://www.ietf.org/rfc/rfc4648.txt) */
object BASE64 : Type {
    override fun validate(value: Any) = value
        .runCatching { getDecoder().decode(toString()); VALID }
        .getOrDefault(INVALID)
}