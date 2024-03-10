package io.github.propactive.type

internal class PORTTest : TypeTestRunner() {
    override val underTest = PORT

    override fun validValues(): Array<Any> = arrayOf(
        "0",
        "1",
        "8080",
        "80",
        "443",
        "1024",
        "65534",
        "49152",
        "49153",
        "65535",
    )

    override fun invalidValues(): Array<Any> = arrayOf(
        "-1",
        "65536",
        "NOT_A_NUMBER",
        "0.0",
        "0.1",
        "%",
        "!",
    )
}
