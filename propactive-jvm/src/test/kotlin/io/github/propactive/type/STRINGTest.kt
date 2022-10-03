package io.github.propactive.type

import java.util.UUID.randomUUID

internal class STRINGTest : TypeTestRunner() {
    override val underTest = STRING

    override fun validValues(): Array<Any> = arrayOf(
        "10", // integer value
        "10.0", // decimal value
        "ABCDE", // string value
        "{}", // special characters value
    )

    override fun invalidValues(): Array<Any> = arrayOf(
        100, // non-string integer
        0.5, // non-string double
        Unit, // Unit value
        randomUUID(), // UUID value
    )
}
