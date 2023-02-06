package io.github.propactive.plugin

import io.github.propactive.matcher.ConfigurationMatcher.Companion.shouldMatch
import io.github.propactive.plugin.Configuration.Companion.DEFAULT_BUILD_DESTINATION
import io.github.propactive.plugin.Configuration.Companion.DEFAULT_CLASS_COMPILE_DEPENDENCY
import io.github.propactive.plugin.Configuration.Companion.DEFAULT_ENVIRONMENTS
import io.github.propactive.plugin.Configuration.Companion.DEFAULT_FILENAME_OVERRIDE
import io.github.propactive.plugin.Configuration.Companion.DEFAULT_IMPLEMENTATION_CLASS
import io.github.propactive.plugin.Propactive.Companion.PROPACTIVE_GROUP
import io.github.propactive.support.utils.alphaNumeric
import io.github.propactive.task.GenerateApplicationPropertiesTask
import io.github.propactive.task.ValidateApplicationPropertiesTask
import io.kotest.extensions.system.OverrideMode.SetOrOverride
import io.kotest.extensions.system.withSystemProperties
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.plugins.ExtensionContainer
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream
import kotlin.random.Random

class PropactiveTest {
    @Nested
    @TestInstance(PER_CLASS)
    inner class Unit {
        private lateinit var underTest: Propactive
        private lateinit var target: Project
        private lateinit var configuration: Configuration
        private lateinit var extensionContainer: ExtensionContainer

        @BeforeEach
        internal fun setUp() {
            underTest = Propactive()
            configuration = mockk(relaxed = true)

            extensionContainer = mockk(relaxed = true) {
                every { findByType(Configuration::class.java) } returns configuration
            }

            target = mockk(relaxed = true) { every { extensions } returns extensionContainer }
            underTest.apply(target)
        }

        @Test
        fun `should register Configuration extension`() {
            verify {
                extensionContainer.create(PROPACTIVE_GROUP, Configuration::class.java)
            }
        }

        @ParameterizedTest
        @MethodSource("allPluginTasksArg")
        fun `should register GenerateApplicationPropertiesTask`(
            taskName: String,
            taskReference: Class<out Task>
        ) {
            verify { target.tasks.register(taskName, taskReference) }
        }

        private fun allPluginTasksArg() = Stream.of(
            Arguments.of(GenerateApplicationPropertiesTask.TASK_NAME, GenerateApplicationPropertiesTask::class.java),
            Arguments.of(ValidateApplicationPropertiesTask.TASK_NAME, ValidateApplicationPropertiesTask::class.java),
        )
    }

    @Nested
    inner class Acceptance {
        private lateinit var project: Project

        @BeforeEach
        internal fun setUp() {
            project = ProjectBuilder.builder()
                .withName("temporary-project-${java.util.UUID.randomUUID()}")
                .build()
                .also { p -> p.plugins.apply("java-library") }
                .also { p -> p.plugins.apply(Propactive::class.java) }
        }

        @Test
        fun `should register task to generate application properties`() {
            project
                .getTasksByName(GenerateApplicationPropertiesTask.TASK_NAME, false)
                .shouldNotBeEmpty()
                .first()
                .shouldBeInstanceOf<GenerateApplicationPropertiesTask>()
                .apply {
                    group shouldBe PROPACTIVE_GROUP
                    description shouldBe GenerateApplicationPropertiesTask.TASK_DESCRIPTION
                }
        }

        @Test
        fun `should register task to validate application properties`() {
            project
                .getTasksByName(ValidateApplicationPropertiesTask.TASK_NAME, false)
                .shouldNotBeEmpty()
                .first()
                .shouldBeInstanceOf<ValidateApplicationPropertiesTask>()
                .apply {
                    group shouldBe PROPACTIVE_GROUP
                    description shouldBe ValidateApplicationPropertiesTask.TASK_DESCRIPTION
                }
        }

        @Test
        fun `should register task configuration extension`() {
            project
                .extensions
                .findByType(Configuration::class.java)
                .shouldNotBeNull()
        }

        @Test
        fun `should provide sane configuration defaults`() {
            project
                .extensions
                .findByType(Configuration::class.java)!!
                .shouldMatch {
                    withEnvironments(DEFAULT_ENVIRONMENTS)
                    withImplementationClass(DEFAULT_IMPLEMENTATION_CLASS)
                    withDestination(DEFAULT_BUILD_DESTINATION)
                    withFilenameOverride(DEFAULT_FILENAME_OVERRIDE)
                    withImplementationClassCompileDependency(DEFAULT_CLASS_COMPILE_DEPENDENCY)
                }
        }

        @Test
        fun `should allow modifying default configurations`() {
            val customEnvironments = Random.alphaNumeric("customEnvironments")
            val customImplementationClass = Random.alphaNumeric("customImplementationClass")
            val customDestination = Random.alphaNumeric("customDestination")
            val customFilenameOverride = Random.alphaNumeric("customFilenameOverride")
            val customImplementationClassCompileDependency = Random.alphaNumeric("customImplementationClassCompileDependency")

            project
                .extensions
                .findByType(Configuration::class.java)!!
                .apply {
                    environments = customEnvironments
                    implementationClass = customImplementationClass
                    destination = customDestination
                    filenameOverride = customFilenameOverride
                    classCompileDependency = customImplementationClassCompileDependency
                }

            project
                .extensions
                .findByType(Configuration::class.java)!!
                .shouldMatch {
                    withEnvironments(customEnvironments)
                    withImplementationClass(customImplementationClass)
                    withDestination(customDestination)
                    withFilenameOverride(customFilenameOverride)
                    withImplementationClassCompileDependency(customImplementationClassCompileDependency)
                }
        }

        @Test
        fun `should allow setting propactive configurations through system properties`() {
            val customEnvironments = Random.alphaNumeric("customEnvironments")
            val customImplementationClass = Random.alphaNumeric("customImplementationClass")
            val customDestination = Random.alphaNumeric("customDestination")
            val customFilenameOverride = Random.alphaNumeric("customFilenameOverride")
            val customImplementationClassCompileDependency = Random.alphaNumeric("customImplementationClassCompileDependency")

            val properties = java.util.Properties().apply {
                put(Configuration::environments.name, customEnvironments)
                put(Configuration::implementationClass.name, customImplementationClass)
                put(Configuration::destination.name, customDestination)
                put(Configuration::filenameOverride.name, customFilenameOverride)
                put(Configuration::classCompileDependency.name, customImplementationClassCompileDependency)
            }

            // 1. set configuration through system properties
            withSystemProperties(properties, SetOrOverride) {
                project
                    .extensions
                    .findByType(Configuration::class.java)!!
                    .apply {
                        environments = customEnvironments
                        implementationClass = customImplementationClass
                        destination = customDestination
                        filenameOverride = customFilenameOverride
                        classCompileDependency = customImplementationClassCompileDependency
                    }
            }

            // 2. assert configurations were set through system properties
            project
                .extensions
                .findByType(Configuration::class.java)!!
                .shouldMatch {
                    withEnvironments(customEnvironments)
                    withImplementationClass(customImplementationClass)
                    withDestination(customDestination)
                    withFilenameOverride(customFilenameOverride)
                    withImplementationClassCompileDependency(customImplementationClassCompileDependency)
                }
        }

        @Test
        fun `should allow overriding propactive custom configurations with system properties`() {
            val customConfigEnvironments = Random.alphaNumeric("customConfigEnvironments")
            val customConfigImplementationClass = Random.alphaNumeric("customConfigImplementationClass")
            val customConfigDestination = Random.alphaNumeric("customConfigDestination")
            val customConfigFilenameOverride = Random.alphaNumeric("customConfigFilenameOverride")
            val customConfigImplementationClassCompileDependency = Random.alphaNumeric("customConfigImplementationClassCompileDependency")

            val customPropertyEnvironments = Random.alphaNumeric("customPropertyEnvironments")
            val customPropertyImplementationClass = Random.alphaNumeric("customPropertyImplementationClass")
            val customPropertyDestination = Random.alphaNumeric("customPropertyDestination")
            val customPropertyFilenameOverride = Random.alphaNumeric("customPropertyFilenameOverride")
            val customPropertyImplementationClassCompileDependency = Random.alphaNumeric("customPropertyImplementationClassCompileDependency")

            // 1. Set custom configuration through extension
            project
                .extensions
                .findByType(Configuration::class.java)!!
                .apply {
                    environments = customConfigEnvironments
                    implementationClass = customConfigImplementationClass
                    destination = customConfigDestination
                    filenameOverride = customConfigFilenameOverride
                    classCompileDependency = customConfigImplementationClassCompileDependency
                }

            // 2. assert custom configuration were set
            project
                .extensions
                .findByType(Configuration::class.java)!!
                .shouldMatch {
                    withEnvironments(customConfigEnvironments)
                    withImplementationClass(customConfigImplementationClass)
                    withDestination(customConfigDestination)
                    withFilenameOverride(customConfigFilenameOverride)
                    withImplementationClassCompileDependency(customConfigImplementationClassCompileDependency)
                }

            val properties = java.util.Properties().apply {
                put(Configuration::environments.name, customPropertyEnvironments)
                put(Configuration::implementationClass.name, customPropertyImplementationClass)
                put(Configuration::destination.name, customPropertyDestination)
                put(Configuration::filenameOverride.name, customPropertyFilenameOverride)
                put(Configuration::classCompileDependency.name, customPropertyImplementationClassCompileDependency)
            }

            // 3. override configuration through system properties
            withSystemProperties(properties, SetOrOverride) {
                project
                    .extensions
                    .findByType(Configuration::class.java)!!
                    .apply {
                        environments = customPropertyEnvironments
                        implementationClass = customPropertyImplementationClass
                        destination = customPropertyDestination
                        filenameOverride = customPropertyFilenameOverride
                        classCompileDependency = customPropertyImplementationClassCompileDependency
                    }
            }

            // 4. assert the configurations were overridden through system properties
            project
                .extensions
                .findByType(Configuration::class.java)!!
                .shouldMatch {
                    withEnvironments(customPropertyEnvironments)
                    withImplementationClass(customPropertyImplementationClass)
                    withDestination(customPropertyDestination)
                    withFilenameOverride(customPropertyFilenameOverride)
                    withImplementationClassCompileDependency(customPropertyImplementationClassCompileDependency)
                }
        }
    }
}
