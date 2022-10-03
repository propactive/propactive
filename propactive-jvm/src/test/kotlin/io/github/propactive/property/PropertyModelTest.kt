package io.github.propactive.property

import nl.jqno.equalsverifier.EqualsVerifier
import nl.jqno.equalsverifier.Warning
import org.junit.jupiter.api.Test

internal class PropertyModelTest {

    @Test
    fun `verify the equals and hashCode contract`() {
        EqualsVerifier
            .configure()
            .suppress(Warning.ALL_FIELDS_SHOULD_BE_USED)
            .forClass(PropertyModel::class.java)
            .verify()
    }
}
