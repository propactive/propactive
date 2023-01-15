package io.github.propactive.plugin

import io.github.propactive.matcher.ConfigurationMatcher.Companion.shouldMatch
import io.github.propactive.plugin.Configuration.Companion.DEFAULT_BUILD_DESTINATION
import io.github.propactive.plugin.Configuration.Companion.DEFAULT_ENVIRONMENTS
import io.github.propactive.plugin.Configuration.Companion.DEFAULT_FILENAME_OVERRIDE
import io.github.propactive.plugin.Configuration.Companion.DEFAULT_IMPLEMENTATION_CLASS
import io.github.propactive.plugin.Configuration.Companion.DEFAULT_CLASS_COMPILE_DEPENDENCY
import io.kotest.matchers.nulls.shouldNotBeNull
import org.junit.jupiter.api.Test
import java.util.UUID.randomUUID

internal class ConfigurationTest {

    @Test
    fun shouldDefaultConstructorToSaneDefaults() {
        Configuration()
            .shouldMatch {
                withImplementationClass(DEFAULT_IMPLEMENTATION_CLASS)
                withDestination(DEFAULT_BUILD_DESTINATION)
                withFilenameOverride(DEFAULT_FILENAME_OVERRIDE)
                withEnvironments(DEFAULT_ENVIRONMENTS)
                withImplementationClassCompileDependency(DEFAULT_CLASS_COMPILE_DEPENDENCY)
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
                classCompileDependency = "${randomUUID()}"
            }.apply {
                environments.shouldNotBeNull()
                implementationClass.shouldNotBeNull()
                destination.shouldNotBeNull()
                filenameOverride.shouldNotBeNull()
                classCompileDependency.shouldNotBeNull()
            }.apply {
                environments = environmentsNewValue
                implementationClass = implementationClassNewValue
                destination = destinationNewValue
                filenameOverride = filenameOverrideNewValue
                classCompileDependency = implementationClassCompileDependencyNewValue
            }.shouldMatch {
                withEnvironments(environmentsNewValue)
                withImplementationClass(implementationClassNewValue)
                withDestination(destinationNewValue)
                withFilenameOverride(filenameOverrideNewValue)
                withImplementationClassCompileDependency(implementationClassCompileDependencyNewValue)
            }
    }
}
