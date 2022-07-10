package propactive.type

internal class BOOLEANTest : TypeTestRunner() {
    override val underTest = BOOLEAN

    override fun validValues(): Array<Any> = arrayOf(
        "True",
        "False",
        "true",
        "false",
        true,
        false,
    )

    override fun invalidValues(): Array<Any> = arrayOf(
        Unit,      // Unit value
        "invalid", // Invalid only
        1234,      // Number
    )
}