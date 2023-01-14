package io.github.propactive.plugin

import io.github.propactive.plugin.Configuration.Companion.DEFAULT_BUILD_DESTINATION
import io.github.propactive.plugin.Configuration.Companion.DEFAULT_ENVIRONMENTS
import io.github.propactive.plugin.Configuration.Companion.DEFAULT_FILENAME_OVERRIDE
import io.github.propactive.plugin.Configuration.Companion.DEFAULT_IMPLEMENTATION_CLASS
import io.github.propactive.plugin.Configuration.Companion.DEFAULT_IMPLEMENTATION_CLASS_COMPILE_DEPENDENCY
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.util.UUID.randomUUID

internal class ConfigurationTest {

    @Test
    fun shouldDefaultConstructorToSaneDefaults() {
        Configuration().apply {
            environments shouldBe DEFAULT_ENVIRONMENTS
            implementationClass shouldBe DEFAULT_IMPLEMENTATION_CLASS
            destination shouldBe DEFAULT_BUILD_DESTINATION
            filenameOverride shouldBe DEFAULT_FILENAME_OVERRIDE
            implementationClassCompileDependency shouldBe DEFAULT_IMPLEMENTATION_CLASS_COMPILE_DEPENDENCY
        }
    }

    @Test
    fun shouldAllowMutabilityForConfigurationFields() {
        val environmentsNewValue = "${randomUUID()}"
        val implementationClassNewValue = "${randomUUID()}"
        val destinationNewValue = "${randomUUID()}"
        val filenameOverrideNewValue = "${randomUUID()}"
        val implementationClassCompileDependencyNewValue = "${randomUUID()}"

        Configuration()
            .apply {
                environments = "${randomUUID()}"
                implementationClass = "${randomUUID()}"
                destination = "${randomUUID()}"
                filenameOverride = "${randomUUID()}"
                implementationClassCompileDependency = "${randomUUID()}"
            }.apply {
                environments.shouldNotBeNull()
                implementationClass.shouldNotBeNull()
                destination.shouldNotBeNull()
                filenameOverride.shouldNotBeNull()
                implementationClassCompileDependency.shouldNotBeNull()
            }.apply {
                environments = environmentsNewValue
                implementationClass = implementationClassNewValue
                destination = destinationNewValue
                filenameOverride = filenameOverrideNewValue
                implementationClassCompileDependency = implementationClassCompileDependencyNewValue
            }.apply {
                environments shouldBe environmentsNewValue
                implementationClass shouldBe implementationClassNewValue
                destination shouldBe destinationNewValue
                filenameOverride shouldBe filenameOverrideNewValue
                implementationClassCompileDependency shouldBe implementationClassCompileDependencyNewValue
            }
    }
}
