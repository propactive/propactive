package io.github.propactive.plugin

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
            filenameOverride.shouldBeNull()
        }
    }

    @Test
    fun shouldAllowMutabilityForConfigurationFields() {
        val environmentsNewValue = "${randomUUID()}"
        val implementationClassNewValue = "${randomUUID()}"
        val destinationNewValue = "${randomUUID()}"
        val filenameOverrideNewValue = "${randomUUID()}"

        Configuration()
            .apply {
                environments = "${randomUUID()}"
                implementationClass = "${randomUUID()}"
                destination = "${randomUUID()}"
                filenameOverride = "${randomUUID()}"
            }.apply {
                environments.shouldNotBeNull()
                implementationClass.shouldNotBeNull()
                destination.shouldNotBeNull()
                filenameOverride.shouldNotBeNull()
            }.apply {
                environments = environmentsNewValue
                implementationClass = implementationClassNewValue
                destination = destinationNewValue
                filenameOverride = filenameOverrideNewValue
            }.apply {
                environments shouldBe environmentsNewValue
                implementationClass shouldBe implementationClassNewValue
                destination shouldBe destinationNewValue
                filenameOverride shouldBe filenameOverrideNewValue
            }
    }
}
