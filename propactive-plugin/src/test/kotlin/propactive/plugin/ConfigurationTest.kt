package propactive.plugin

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.util.UUID.randomUUID

internal class ConfigurationTest {

    @Test
    fun shouldDefaultConstructorToNullValues() {
        Configuration().apply {
            environments.shouldBeNull()
            implementationClass.shouldBeNull()
            destination.shouldBeNull()
        }
    }

    @Test
    fun shouldAllowMutabilityForConfigurationFields() {
        val environmentsNewValue = "${randomUUID()}"
        val implementationClassNewValue = "${randomUUID()}"
        val destinationNewValue = "${randomUUID()}"

        Configuration()
            .apply {
                environments = "${randomUUID()}"
                implementationClass = "${randomUUID()}"
                destination = "${randomUUID()}"
            }.apply {
                environments.shouldNotBeNull()
                implementationClass.shouldNotBeNull()
                destination.shouldNotBeNull()
            }.apply {
                environments = environmentsNewValue
                implementationClass = implementationClassNewValue
                destination = destinationNewValue
            }.apply {
                environments shouldBe environmentsNewValue
                implementationClass shouldBe implementationClassNewValue
                destination shouldBe destinationNewValue
            }
    }
}