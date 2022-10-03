package io.github.propactive.type

internal class URLTest : TypeTestRunner() {
    override val underTest = URL

    override fun validValues(): Array<Any> = arrayOf(
        "http://foo.bar.baz/qux", // http  URL with endpoint
        "https://foo.bar.baz:443", // https URL with port number
        "https://foo.bar.baz/qux?quux=corge", // https URL with query string
        "http://[1080::8:800:200C:417A]/foo", // literal IPv6 address
        "http://[::192.9.5.5]/ipng", // literal IPv4 address
        // Other supported protocols:
        "file://foo.bar.baz", // file
        "ftp://foo.bar.baz", // ftp
        "mailto://foo.bar.baz", // mailto
    )

    override fun invalidValues(): Array<Any> = arrayOf(
        "localhost", // localhost
        "localhost:8080", // localhost with port number
        "localhost:8080/endpoint", // localhost with port number and endpoint
        "abcd.efg", // URL without www
        "www.abcd.efg", // URL without protocol
        100, // non-string integer
        0.5, // non-string double
        Unit, // Unit value
    )
}
