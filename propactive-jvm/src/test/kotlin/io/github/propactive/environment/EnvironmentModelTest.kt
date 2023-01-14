package io.github.propactive.environment

import io.github.propactive.property.PropertyModel
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
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

    @Test
    fun `should be equal when the name is the same`() {
        val sameName = "sameName"
        val sameFilename = "sameFilename"
        val sameProperties = setOf<PropertyModel>()

        val environmentA = EnvironmentModel(sameName, sameFilename, sameProperties)
        val environmentB = EnvironmentModel(sameName, sameFilename, sameProperties)

        environmentA shouldBe environmentB
    }

    @Test
    fun `should not be equal when the name is different`() {
        val sameFilename = "sameFilename"
        val sameProperties = setOf<PropertyModel>()

        val environmentA = EnvironmentModel("name", sameFilename, sameProperties)
        val environmentB = EnvironmentModel("differentName", sameFilename, sameProperties)

        environmentA shouldNotBe environmentB
    }
}
