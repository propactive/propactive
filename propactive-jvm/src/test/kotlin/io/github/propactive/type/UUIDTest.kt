package io.github.propactive.type

internal class UUIDTest : TypeTestRunner() {
    override val underTest = UUID

    override fun validValues(): Array<Any> = arrayOf(
        "00000000-0000-0000-0000-000000000000", // Nil/Empty UUID
        "1ed0a470-bb84-11ec-ae36-00163e9b33ca", // version 1
        "2516fc5f-c950-3ac0-809c-25dfee9b2cd6", // version 3
        "fce3221e-782e-4d71-ad12-ca57ea0ba045", // version 4
        "45217730-24d1-559a-8248-33978c7a0c61", // version 5
        java.util.UUID.randomUUID() // Random UUID (v4)
    )

    override fun invalidValues(): Array<Any> = arrayOf(
        "invalid", // invalid value
        100, // non-string integer
        0.5, // non-string double
        Unit, // Unit value
    )
}
