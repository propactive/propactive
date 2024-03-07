package io.github.propactive.type

import io.github.propactive.type.Type.Companion.INVALID
import io.github.propactive.type.Type.Companion.VALID

/**
 * URI type as defined by [RFC 3986](https://www.ietf.org/rfc/rfc3986.txt)
 *
 * Example of valid values:
 * - localhost
 * - localhost:8080
 * - localhost:8080/endpoint
 * - jdbc:foo:bar:@baz:1521:qux
 * - amqp://foo?bar=5&baz=10000
 * - app://foo.bar.baz/qux
 * - slack://foo?bar=baz
 *
 * NOTE:
 *  For well-formed HTTP and HTTPs URIs, please use the stricter type, URL.
 *  @see io.github.propactive.type.URL
 */
object URI : Type {
    override fun validate(value: Any) = value
        .runCatching { java.net.URI(toString()); VALID }
        .getOrDefault(INVALID)
}
