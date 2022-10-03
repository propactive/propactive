package io.github.propactive.type

internal class URITest : TypeTestRunner() {
    override val underTest = URI

    override fun validValues(): Array<Any> = arrayOf(
        "localhost", // localhost
        "localhost:8080", // localhost with port number
        "localhost:8080/endpoint", // localhost with port number and endpoint
        // Common URI schemes:
        "jdbc:foo:bar:@baz:1521:qux", // jdbc
        "amqp://foo?bar=5&baz=10000", // amqp
        "app://foo.bar.baz/qux", // app (Google)
        "slack://foo?bar=baz", // slack
        "tel:+1-816-555-1212", // tel
    )

    override fun invalidValues(): Array<Any> = arrayOf(
        ":foo", // Invalid URI syntax
        "foo\\bar", // Invalid URI syntax
        "foo.bar|baz:8080", // Invalid URI syntax
        "[::1.2.3]", // Invalid URI syntax
    )
}
