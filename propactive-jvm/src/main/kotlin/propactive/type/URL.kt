package propactive.type

import propactive.type.Type.Companion.INVALID
import propactive.type.Type.Companion.VALID

/**
 * URL type as defined by [RFC 2396](https://www.ietf.org/rfc/rfc2396.txt)
 *
 * NOTE:
 *  If you want to use "localhost" or other unsupported protocols,
 *  please use the URI type which is more lenient:
 *  @see propactive.type.URI
 */
object URL : Type {
    override fun validate(value: Any) = value
        .runCatching { java.net.URL(toString()); VALID }
        .getOrDefault(INVALID)
}