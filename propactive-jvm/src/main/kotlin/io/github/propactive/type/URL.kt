package io.github.propactive.type

import io.github.propactive.type.Type.Companion.INVALID
import io.github.propactive.type.Type.Companion.VALID

/**
 * URL type as defined by [RFC 2396](https://www.ietf.org/rfc/rfc2396.txt)
 *
 * NOTE:
 *  If you want to use "localhost" or other unsupported protocols,
 *  please use the URI type which is more lenient:
 *  @see io.github.propactive.type.URI
 */
object URL : Type {
    override fun validate(value: Any) = value
        .runCatching { java.net.URI(toString()).toURL(); VALID }
        .getOrDefault(INVALID)
}
