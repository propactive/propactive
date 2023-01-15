package io.github.propactive.environment

import io.github.propactive.config.DEFAULT_ENVIRONMENT_FILENAME
import io.github.propactive.config.UNSPECIFIED_ENVIRONMENT
import io.github.propactive.entry.EntryModel
import io.github.propactive.environment.EnvironmentFailureReason.ENVIRONMENT_INVALID_KEY_EXPANSION
import io.github.propactive.matcher.EnvironmentMatcher.Companion.shouldMatch
import io.github.propactive.matcher.PropertyMatcher.Companion.propertyMatcher
import io.github.propactive.property.Property
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class EnvironmentFactoryTest {

    @Nested
    inner class HappyPath {
        @Test
        fun `given an empty environment object, when factory creates a DAO, then it should provide default filename`() {
            EnvironmentFactory
                .create(Empty::class)
                .apply { this shouldHaveSize 1 }
                .first()
                .shouldMatch {
                    withName(UNSPECIFIED_ENVIRONMENT)
                    withFilename(DEFAULT_ENVIRONMENT_FILENAME)
                    withoutProperties()
                }
        }

        @Test
        fun `given a custom filename for a single environment only, when factory creates a DAO, then it should use the given filename`() {
            EnvironmentFactory
                .create(WithSingleFilename::class)
                .apply { this shouldHaveSize 1 }
                .first()
                .shouldMatch {
                    withName(UNSPECIFIED_ENVIRONMENT)
                    withFilename("test-application.properties")
                    withoutProperties()
                }
        }

        @Test
        fun `given WithDifferentEnvironmentValues, when factory creates a DAO, then it should do the correct value associations`() {
            EnvironmentFactory
                .create(WithDifferentEnvironmentValues::class)
                .apply {
                    this shouldHaveSize 2
                    this.toList().forEachIndexed { index, environment ->
                        environment.shouldMatch {
                            withName("env$index")
                            withFilename("env$index-application.properties")
                            withProperties(
                                propertyMatcher()
                                    .withName("test.resource.value")
                                    .withEnvironment("env$index")
                                    .withValue("env${index}Value"),
                            )
                        }
                    }
                }
        }

        @Test
        fun `given WithMultipleProperties, when factory creates a DAO, then it should do the correct value associations`() {
            EnvironmentFactory
                .create(WithMultipleProperties::class)
                .apply { this shouldHaveSize 1 }
                .first()
                .shouldMatch {
                    withName(UNSPECIFIED_ENVIRONMENT)
                    withFilename("env-application.properties")
                    withProperties(
                        propertyMatcher()
                            .withName(WithMultipleProperties.property1)
                            .withEnvironment(UNSPECIFIED_ENVIRONMENT)
                            .withValue("property1Value"),
                        propertyMatcher()
                            .withName(WithMultipleProperties.property2)
                            .withEnvironment(UNSPECIFIED_ENVIRONMENT)
                            .withValue("property2Value"),
                    )
                }
        }

        @Test
        fun `given WithEnvironmentKeyExpansion, when factory creates a DAO, then it should correctly expand key values`() {
            EnvironmentFactory
                .create(WithEnvironmentKeyExpansion::class)
                .apply {
                    this shouldHaveSize 2
                    this.toList().forEachIndexed { index, environment ->
                        environment.shouldMatch {
                            withName("env$index")
                            withFilename("env$index-application.properties")
                            withoutProperties()
                        }
                    }
                }
        }
    }

    @Nested
    inner class SadPath {
        @Test
        fun `given an object MissingEnvironmentAnnotation, when factory creates DAO, then it should error`() {
            assertThrows<IllegalArgumentException> {
                EnvironmentFactory
                    .create(MissingEnvironmentAnnotation::class)
            }.message shouldBe EnvironmentFailureReason.ENVIRONMENT_MISSING_ANNOTATION()
        }

        @Test
        fun `given an environment with WithEnvironmentKeyExpansionButMissingWildcard, when factory creates a DAO, then it should error`() {
            assertThrows<IllegalArgumentException> {
                EnvironmentFactory
                    .create(WithEnvironmentKeyExpansionButMissingWildcard::class)
            }.message shouldBe ENVIRONMENT_INVALID_KEY_EXPANSION(EntryModel("env0/env1", " -application.properties"))()
        }
    }

    // HAPPY PATH OBJECTS

    @Environment
    object Empty

    @Environment([":test-application.properties"])
    object WithSingleFilename

    @Environment(["env0: env0-application.properties", "env1: env1-application.properties"])
    object WithDifferentEnvironmentValues {
        @Property(["env0:env0Value", "env1:env1Value"])
        const val property = "test.resource.value"
    }

    @Environment(["env-application.properties"])
    object WithMultipleProperties {
        @Property(["property1Value"])
        const val property1 = "test.resource.property1"

        @Property(["property2Value"])
        const val property2 = "test.resource.property2"
    }

    @Environment(["env0/env1: *-application.properties"])
    object WithEnvironmentKeyExpansion

    // SAD PATH OBJECTS

    object MissingEnvironmentAnnotation

    @Environment(["env0/env1: -application.properties"])
    object WithEnvironmentKeyExpansionButMissingWildcard
}
