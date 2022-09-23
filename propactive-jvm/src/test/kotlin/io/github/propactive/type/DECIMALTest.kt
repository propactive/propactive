package io.github.propactive.type


internal class DECIMALTest : TypeTestRunner() {
    override val underTest = DECIMAL

    override fun validValues(): Array<Any> = arrayOf(
        "0.5",  // String decimal
        "10.0", // Another string decimal
        "1",    // String integer (accepted)
        0.5,    // Non-string decimal
        10.0,   // Another non-string decimal
        1,      // Non-string integer (accepted)
    )

    override fun invalidValues(): Array<Any> = arrayOf(
        "invalid", // invalid value
        Unit,      // Unit value
    )
}