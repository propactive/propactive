package io.github.propactive.type

internal class INTEGERTest : TypeTestRunner() {
    override val underTest = INTEGER

    override fun validValues(): Array<Any> = arrayOf(
        "01", // integer with leading zero
        "11", // simple integer
        1111, // non-string integer
        -111, // non-string negative integer
    )

    override fun invalidValues(): Array<Any> = arrayOf(
        "invalid", // invalid value
        "0xFF", // hex value
        "10.0", // decimal value
        0.05, // non-string decimal value
        Unit, // Unit value
    )
}
