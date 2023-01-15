package io.github.propactive.integration

import io.github.propactive.environment.Environment
import io.github.propactive.plugin.Configuration
import io.github.propactive.plugin.Configuration.Companion.DEFAULT_BUILD_DESTINATION
import io.github.propactive.plugin.Configuration.Companion.DEFAULT_ENVIRONMENTS
import io.github.propactive.plugin.Configuration.Companion.DEFAULT_IMPLEMENTATION_CLASS
import io.github.propactive.plugin.Propactive.Companion.PROPACTIVE_GROUP
import io.github.propactive.property.Property
import io.github.propactive.support.tasks.PluginTask.GENERATE_TASK
import io.github.propactive.support.tasks.PluginTask.VALIDATE_TASK
import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.matchers.string.shouldContain
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.UnexpectedBuildFailure
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.io.File
import java.nio.file.Files
import kotlin.text.RegexOption.DOT_MATCHES_ALL

class PropactiveTest {
    @Nested
    @TestInstance(PER_CLASS)
    inner class Integration {
        private lateinit var projectDir: File

        @BeforeEach
        fun setUp() {
            projectDir = Files
                .createTempDirectory("projectDir")
                .toFile()
        }

        @AfterEach
        fun tearDown() {
            projectDir.delete()
        }

        @Test
        fun `should display propactive tasks description`() {
            projectDir.also { parent ->
                File(parent, "build.gradle.kts")
                    .apply {
                        writeText(
                            """
                            | plugins {
                            |     id("java-library")
                            |     id("io.github.propactive") version "DEV-SNAPSHOT"
                            | }
                            """.trimMargin(),
                        )
                    }
            }

            GradleRunner
                .create()
                .withProjectDir(projectDir)
                .withArguments("tasks")
                .withPluginClasspath()
                .build()
                .apply {
                    output shouldContain """
                    Propactive tasks
                    ----------------
                    ${GENERATE_TASK.taskName} - .*?

                    ${VALIDATE_TASK.taskName} - .*?
                    """.trimIndent().toRegex(DOT_MATCHES_ALL)
                }
        }

        @Test
        fun `should register configuration extension for propactive plugin`() {
            projectDir.also { parent ->
                File(parent, "build.gradle.kts")
                    .apply {
                        writeText(
                            """
                            | plugins {
                            |     id("io.github.propactive") version "DEV-SNAPSHOT"
                            | }
                            |
                            | $PROPACTIVE_GROUP {
                            |     ${Configuration::destination.name} = "$DEFAULT_BUILD_DESTINATION"
                            |     ${Configuration::implementationClass.name} = "$DEFAULT_IMPLEMENTATION_CLASS"
                            |     ${Configuration::environments.name} = "$DEFAULT_ENVIRONMENTS"
                            | }
                            """.trimMargin(),
                        )
                    }
            }

            shouldNotThrow<UnexpectedBuildFailure> {
                GradleRunner
                    .create()
                    .withProjectDir(projectDir)
                    .withPluginClasspath()
                    .build()
            }
        }

        @ParameterizedTest
        @CsvSource("1.1.2", "1.1.4") // TODO: add "1.8.0, DEV-SNAPSHOT" case...
        fun `should be able to generate application properties for a valid setup e2e with current Kotlin version`(propactiveVersion: String) {
            val envName = "dev"
            val envFilename = "dev-application.properties"
            val propertyKey = "propactive.dev.string.key"
            val propertyValue = "stringValue"

            // https://docs.gradle.org/current/userguide/test_kit.html#sub:test-kit-automatic-classpath-injection
            projectDir.also { parent ->
                // TODO:
                //  - see if you can simplify this file
                //  - I believe you can get snapshot builds for the plugin and dependencies
                //    using: https://docs.gradle.org/current/userguide/declaring_repositories.html#sec:declaring_custom_repository
                //    See also: https://docs.gradle.org/current/userguide/declaring_repositories.html#sec:repository-types
                //  - So technically you only need to point this kts to build/libs/propactive-DEV-SNAPSHOT.jar
                //  - Which means we might need to extract this test to a separate module (i.e. so we can build the plugin/dep jars first)
                File(parent, "build.gradle.kts")
                    .apply {
                        writeText(
                            """
                            | import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
                            |
                            | plugins {
                            |     kotlin("jvm") version "${KotlinVersion.CURRENT}"
                            |
                            |     id("java-library")
                            |     id("io.github.propactive") version "1.1.2"
                            | }
                            |
                            | $PROPACTIVE_GROUP {
                            |     ${Configuration::destination.name} = "$DEFAULT_BUILD_DESTINATION"
                            |     ${Configuration::implementationClass.name} = "$DEFAULT_IMPLEMENTATION_CLASS"
                            |     ${Configuration::environments.name} = "$DEFAULT_ENVIRONMENTS"
                            | }
                            |
                            | repositories {
                            |     mavenCentral()
                            | }
                            |
                            | dependencies {
                            |     implementation("io.github.propactive:propactive-jvm:1.1.2")
                            | }
                            |
                            | tasks {
                            |     withType<KotlinCompile>().configureEach {
                            |          kotlinOptions.jvmTarget = "17"
                            |     }
                            | }
                            |
                            | java {
                            |     toolchain {
                            |         languageVersion.set(JavaLanguageVersion.of(17))
                            |     }
                            | }
                            """.trimMargin(),
                        )
                    }

                File(parent, "src/main/kotlin/$DEFAULT_IMPLEMENTATION_CLASS.kt")
                    .apply { parentFile.mkdirs() }
                    .apply {
                        writeText(
                            """
                            | import io.github.propactive.property.Property
                            | import io.github.propactive.environment.Environment
                            |
                            | @${Environment::class.simpleName}([ "$envName: $envFilename" ])
                            | object ApplicationProperties {
                            |     @${Property::class.simpleName}([ "$envName: $propertyValue" ])
                            |     const val stringPropertyKey = "$propertyKey"
                            | }
                            """.trimMargin(),
                        )
                    }
            }

            // TODO:
            //  - assert on tasks output (i.e. the file generted should exist)
            //  - test this on 1.1.2 first and then the broken versions
            shouldNotThrow<UnexpectedBuildFailure> {
                GradleRunner
                    .create()
                    .withProjectDir(projectDir)
                    .withArguments(GENERATE_TASK.taskName)
                    .withPluginClasspath()
                    .build()
            }
        }

        // TODO
        /*
        Write a test that will:
          - Create a gradle kts w/ java-lib, kts-lib...etc.
          - Create a file within src/main/kotlin/group/path/props
          - Hit the jar command
          - Hit the validate task (for valdiate test)
          - Hit the generate task (for generate test)
          - Assertion-wise:
            - Assert that a prop file has been created
            - Assert that it's correct names, correct configs...etc.
          - Ensure correct clean up jobs afterwards
        */
    }
}
