package io.github.propactive.plugin

import io.github.propactive.task.GenerateApplicationProperties
import io.github.propactive.task.GenerateApplicationProperties.DEFAULT_BUILD_DESTINATION
import io.github.propactive.task.GenerateApplicationProperties.DEFAULT_ENVIRONMENTS
import io.github.propactive.task.GenerateApplicationProperties.DEFAULT_IMPLEMENTATION_CLASS
import io.github.propactive.task.GenerateApplicationPropertiesTask
import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.mockk
import io.mockk.verify
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.UnexpectedBuildFailure
import org.junit.jupiter.api.*
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import java.io.File
import java.nio.file.Files
import java.util.*
import kotlin.text.RegexOption.DOT_MATCHES_ALL

class PropactiveTest {
    @Nested
    inner class Unit {
        private lateinit var underTest: Propactive
        private lateinit var target: Project

        @BeforeEach
        internal fun setUp() {
            underTest = Propactive()
            target = mockk(relaxed = true)
            underTest.apply(target)
        }

        @Test
        fun `should register Configuration extension`() {
            verify {
                target
                    .extensions
                    .create(Propactive::class.simpleName!!.lowercase(), Configuration::class.java)
            }
        }

        @Test
        fun `should register GenerateApplicationProperties task`() {
            verify {
                target
                    .tasks
                    .register(
                        GenerateApplicationProperties::class.simpleName!!.replaceFirstChar(Char::lowercaseChar),
                        GenerateApplicationPropertiesTask::class.java,
                        any()
                    )
            }
        }
    }

    @Nested
    inner class Acceptance {
        private lateinit var project: Project

        @BeforeEach
        internal fun setUp() {
            project = ProjectBuilder.builder()
                .withName("temporary-project-${UUID.randomUUID()}")
                .build()
                .also { p -> p.plugins.apply(Propactive::class.java) }
        }

        @Test
        fun `should register task to generate application properties`() {
            project
                .getTasksByName(GenerateApplicationProperties.TASK_NAME, false)
                .shouldNotBeEmpty()
                .first()
                .shouldBeInstanceOf<GenerateApplicationPropertiesTask>()
        }

        @Test
        fun `should provide sane propactive configuration defaults`() {
            project
                .getTasksByName(GenerateApplicationProperties.TASK_NAME, false)
                .first()
                .let { it as GenerateApplicationPropertiesTask }
                .apply {
                    destination shouldBe DEFAULT_BUILD_DESTINATION
                    environments shouldBe DEFAULT_ENVIRONMENTS
                    implementationClass shouldBe DEFAULT_IMPLEMENTATION_CLASS
                }
        }

        @Test
        fun `should register task extension to configure propactive`() {
            project
                .extensions
                .findByType(Configuration::class.java)
                .shouldNotBeNull()
        }

        @Test
        fun `should allow modifying propactive configurations`() {
            val customDestination = "custom/path"
            val customEnvironments = "test"
            val customImplementationClass = "io.github.propactive.Test"

            project
                .extensions
                .findByType(Configuration::class.java)!!
                .apply {
                    destination = customDestination
                    environments = customEnvironments
                    implementationClass = customImplementationClass
                }

            project
                .getTasksByName(GenerateApplicationProperties.TASK_NAME, false)
                .first()
                .let { it as GenerateApplicationPropertiesTask }
                .apply {
                    destination shouldBe customDestination
                    environments shouldBe customEnvironments
                    implementationClass shouldBe customImplementationClass
                }
        }

        @Test
        fun `should allow setting propactive configurations through system property`() {
            val customDestination = "custom/path"
            val customEnvironments = "test"
            val customImplementationClass = "io.github.propactive.Test"

            project
                .getTasksByName(GenerateApplicationProperties.TASK_NAME, false)
                .first()
                .let { it as GenerateApplicationPropertiesTask }
                .apply {
                    setProperty(Configuration::destination.name, customDestination)
                    setProperty(Configuration::environments.name, customEnvironments)
                    setProperty(Configuration::implementationClass.name, customImplementationClass)
                }
                .apply {
                    destination shouldBe customDestination
                    environments shouldBe customEnvironments
                    implementationClass shouldBe customImplementationClass
                }
        }

        @Test
        fun `should allow overriding propactive configurations`() {
            val customConfigDestination = "custom/path/config"
            val customConfigEnvironments = "testConfig"
            val customConfigImplementationClass = "io.github.propactive.TestConfig"

            val customPropertyDestination = "custom/path/Property"
            val customPropertyEnvironments = "testProperty"
            val customPropertyImplementationClass = "io.github.propactive.TestProperty"

            project
                .extensions
                .findByType(Configuration::class.java)!!
                .apply {
                    destination = customConfigDestination
                    environments = customConfigEnvironments
                    implementationClass = customConfigImplementationClass
                }

            project
                .getTasksByName(GenerateApplicationProperties.TASK_NAME, false)
                .first()
                .let { it as GenerateApplicationPropertiesTask }
                .apply {
                    setProperty(Configuration::destination.name, customPropertyDestination)
                    setProperty(Configuration::environments.name, customPropertyEnvironments)
                    setProperty(Configuration::implementationClass.name, customPropertyImplementationClass)
                }
                .apply {
                    destination shouldBe customPropertyDestination
                    environments shouldBe customPropertyEnvironments
                    implementationClass shouldBe customPropertyImplementationClass
                }
        }
    }

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
                            |     id("io.github.propactive") version "DEV-SNAPSHOT"
                            | }
                            """.trimMargin()
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
                    generateApplicationProperties - .*?
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
                            | propactive {
                            |     destination = layout.buildDirectory.dir("properties").get().asFile.absolutePath
                            |     implementationClass = "propactive.dev.Properties"
                            |     environments = "dev"
                            | }
                            """.trimMargin()
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
    }
}