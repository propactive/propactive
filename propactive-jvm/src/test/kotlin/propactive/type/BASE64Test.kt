package propactive.type

internal class BASE64Test : TypeTestRunner() {
    override val underTest = BASE64

    override fun validValues(): Array<Any> = arrayOf(
        "Zm9v",                            // foo
        "Zm9vYmFy",                        // foobar
        "Zm9vYmFyYmF6",                    // foobarbaz
        "aHR0cHM6Ly93d3cuZm9vYmFyLmJhei8", // https://www.foobar.baz/
    )

    override fun invalidValues(): Array<Any> = arrayOf(
        Unit,      // Unit value
        " ",       // Whitespace only
        "Zm@9v!",  // Special characters
        "====",    // Padding only
        "123 456", // Invalid padding
    )
}