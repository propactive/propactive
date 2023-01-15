package io.github.propactive.property

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
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

    @Test
    fun `should be equal when the name and environment are the same`() {
        val sameName = "sameName"
        val sameEnvironment = "sameEnvironment"

        val propertyA = PropertyModel(sameName, sameEnvironment, "value")
        val propertyB = PropertyModel(sameName, sameEnvironment, "differentValue")

        propertyA shouldBe propertyB
    }

    @Test
    fun `should not be equal when the name is different`() {
        val sameEnvironment = "sameEnvironment"
        val sameValue = "sameValue"

        val propertyA = PropertyModel("Name", sameEnvironment, sameValue)
        val propertyB = PropertyModel("differentName", sameEnvironment, sameValue)

        propertyA shouldNotBe propertyB
    }

    @Test
    fun `should not be equal when the environment is different`() {
        val sameName = "sameName"
        val sameValue = "sameValue"

        val propertyA = PropertyModel(sameName, "environment", sameValue)
        val propertyB = PropertyModel(sameName, "differentEnvironment", sameValue)

        propertyA shouldNotBe propertyB
    }
}
