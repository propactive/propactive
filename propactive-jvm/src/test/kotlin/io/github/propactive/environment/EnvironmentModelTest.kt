package io.github.propactive.environment

import nl.jqno.equalsverifier.EqualsVerifier
import nl.jqno.equalsverifier.Warning
import org.junit.jupiter.api.Test

internal class EnvironmentModelTest {

    @Test
    fun `verify the equals and hashCode contract`() {
        EqualsVerifier
            .configure()
            .suppress(Warning.ALL_FIELDS_SHOULD_BE_USED)
            .forClass(EnvironmentModel::class.java)
            .verify()
    }
}