package io.github.propactive.environment

import io.github.propactive.config.DEFAULT_ENVIRONMENT_FILENAME
import io.github.propactive.entry.EntryModel
import io.github.propactive.environment.EnvironmentFailureReason.ENVIRONMENT_INVALID_KEY_EXPANSION
import io.github.propactive.property.Property
import io.kotest.matchers.collections.shouldBeEmpty
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
                .apply {
                    this shouldHaveSize 1
                    this.first().apply {
                        filename shouldBe DEFAULT_ENVIRONMENT_FILENAME
                        properties.shouldBeEmpty()
                    }
                }
        }

        @Test
        fun `given a custom filename for a single environment only, when factory creates a DAO, then it should use the given filename`() {
            EnvironmentFactory
                .create(WithSingleFilename::class)
                .apply {
                    this shouldHaveSize 1
                    this.first().apply {
                        filename shouldBe "test-application.properties"
                        properties.shouldBeEmpty()
                    }
                }
        }

        @Test
        fun `given WithMultipleEnvironments, when factory creates a DAO, then it should do the correct value associations`() {
            EnvironmentFactory
                .create(WithDifferentEnvironmentValues::class)
                .apply {
                    this shouldHaveSize 2
                    this.toList().forEachIndexed { index, model ->
                        model.filename shouldBe "env$index-application.properties"
                        model.properties.first().apply {
                            name shouldBe "test.resource.value"
                            environment shouldBe "env$index"
                            value shouldBe "env${index}Value"
                        }
                    }
                }
        }

        @Test
        fun `given WithEnvironmentKeyExpansion, when factory creates a DAO, then it should do the correctly expand key values`() {
            EnvironmentFactory
                .create(WithEnvironmentKeyExpansion::class)
                .apply {
                    this shouldHaveSize 2
                    this.toList().forEachIndexed { index, model ->
                        model.filename shouldBe "env$index-application.properties"
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

    @Environment(["env0/env1: *-application.properties"])
    object WithEnvironmentKeyExpansion

    // SAD PATH OBJECTS

    object MissingEnvironmentAnnotation

    @Environment(["env0/env1: -application.properties"])
    object WithEnvironmentKeyExpansionButMissingWildcard
}
