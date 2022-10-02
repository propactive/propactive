package io.github.propactive.task

import io.github.propactive.environment.Environment
import io.github.propactive.plugin.Configuration
import io.github.propactive.property.Property
import io.github.propactive.task.GenerateApplicationProperties.DEFAULT_BUILD_DESTINATION
import io.github.propactive.task.GenerateApplicationProperties.DEFAULT_ENVIRONMENTS
import io.github.propactive.task.GenerateApplicationProperties.DEFAULT_FILENAME_OVERRIDE
import io.github.propactive.task.GenerateApplicationProperties.DEFAULT_IMPLEMENTATION_CLASS
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import org.gradle.api.Project
import org.gradle.api.Task
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.fail
import java.io.File
import java.net.URLClassLoader
import java.nio.file.Files
import java.util.UUID

internal class GenerateApplicationPropertiesTest {
    @Test
    fun shouldErrorWhenImplementationClassIsNotFound() {
        val jarTask = mockk<Task>() {
            every { outputs.files.files } returns setOf()
        }

        val project = mockk<Project>() {
            every { getTasksByName("jar", true) } returns setOf(jarTask)
        }

        assertThrows<IllegalStateException> {
            GenerateApplicationProperties.invoke(
                project,
                DEFAULT_ENVIRONMENTS,
                DEFAULT_IMPLEMENTATION_CLASS,
                DEFAULT_BUILD_DESTINATION,
                DEFAULT_FILENAME_OVERRIDE
            )
        }.message shouldBe "Expected to find implementation class $DEFAULT_IMPLEMENTATION_CLASS"
    }

    @Test
    fun shouldCreatePropertyFilesWhenImplementationClassIsFound() {
        setupScenario(WithMultipleEnvironments::class.java) { project, buildDir ->
            val implementationClass =
                "io.github.propactive.task.GenerateApplicationPropertiesTest.WithMultipleEnvironments"

            GenerateApplicationProperties.invoke(
                project,
                DEFAULT_ENVIRONMENTS,
                implementationClass,
                buildDir.absolutePath,
                DEFAULT_FILENAME_OVERRIDE
            )

            buildDir.listFiles()?.apply {
                count() shouldBe 2
                sortBy { it.name }
            }?.forEachIndexed { index, file ->
                file.name shouldBe "env$index-application.properties"
                file?.readLines()?.first() shouldBe "test.resource.value=env${index}Value"
            } ?: fail("Expected to find 2 property files")
        }
    }

    @Test
    fun shouldAllowApplicationPropertiesFilenameOverrideWhenASingularEnvironmentApplicationPropertiesHasBeenGenerated() {
        setupScenario(WithSingularEnvironment::class.java) { project, buildDir ->
            val implementationClass =
                "io.github.propactive.task.GenerateApplicationPropertiesTest.WithSingularEnvironment"

            val customFilename = "${UUID.randomUUID()}-application.properties"

            GenerateApplicationProperties.invoke(
                project,
                DEFAULT_ENVIRONMENTS,
                implementationClass,
                buildDir.absolutePath,
                customFilename
            )

            buildDir
                .listFiles()
                ?.single()
                ?.apply {
                    name shouldBe customFilename
                    readLines().first() shouldBe "test.resource.value=envValue"
                }
        }
    }

    @Test
    fun shouldFailIfApplicationPropertiesFilenameIsSuppliedWhenMoreThanOneEnvironmentIsRequested() {
        setupScenario(WithMultipleEnvironments::class.java) { project, buildDir ->
            val implementationClass =
                "io.github.propactive.task.GenerateApplicationPropertiesTest.WithMultipleEnvironments"

            val customFilename = "${UUID.randomUUID()}-application.properties"

            shouldThrow<IllegalArgumentException> {
                GenerateApplicationProperties.invoke(
                    project,
                    DEFAULT_ENVIRONMENTS,
                    implementationClass,
                    buildDir.absolutePath,
                    customFilename
                )
            }.message shouldBe """
                Received Property to override filename (-P${Configuration::filenameOverride.name}): $customFilename
                However, this can only be used when a single environment is requested/generated. (Tip: Use -P${Configuration::environments.name} to specify environment application properties to generate)
            """.trimIndent()
        }
    }

    @Environment
    object WithSingularEnvironment {
        @Property(["envValue"])
        const val property = "test.resource.value"
    }

    @Environment(["env0: env0-application.properties", "env1: env1-application.properties"])
    object WithMultipleEnvironments {
        @Property(["env0:env0Value", "env1:env1Value"])
        const val property = "test.resource.value"
    }

    private fun setupScenario(
        propertiesClass: Class<*>,
        callback: (Project, File) -> Unit
    ) {
        mockkStatic(URLClassLoader::class) {
            val buildDir = Files
                .createTempDirectory(DEFAULT_BUILD_DESTINATION)
                .toFile().apply { deleteOnExit() }

            val task = mockk<Task> {
                every { outputs.files.files } returns setOf(mockk(relaxed = true))
            }

            val project = mockk<Project>() {
                every { getTasksByName("jar", true) } returns setOf(task)
                every<String> {
                    layout.buildDirectory.dir(buildDir.absolutePath).get().asFile.absolutePath
                } returns buildDir.absolutePath
            }

            every { URLClassLoader.newInstance(any(), any()) } returns mockk {
                every { loadClass(any()) } returns propertiesClass
            }

            callback.invoke(project, buildDir)
        }
    }
}
