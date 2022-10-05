package io.github.propactive.task

import io.github.propactive.environment.Environment
import io.github.propactive.plugin.Configuration
import io.github.propactive.project.ImplementationClassFinder
import io.github.propactive.property.Property
import io.github.propactive.task.GenerateApplicationProperties.DEFAULT_BUILD_DESTINATION
import io.github.propactive.task.GenerateApplicationProperties.DEFAULT_ENVIRONMENTS
import io.github.propactive.task.GenerateApplicationProperties.DEFAULT_FILENAME_OVERRIDE
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import org.gradle.api.Project
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import java.io.File
import java.nio.file.Files
import java.util.*
import kotlin.reflect.KClass

internal class GenerateApplicationPropertiesTest {
    @Test
    fun shouldCreatePropertyFilesWhenImplementationClassIsFound() {
        setupScenario(WithMultipleEnvironments::class) { project, buildDir ->
            GenerateApplicationProperties.invoke(
                project,
                DEFAULT_ENVIRONMENTS,
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
    fun shouldFailIfApplicationPropertiesFilenameIsSuppliedWhenMoreThanOneEnvironmentIsRequested() {
        setupScenario(WithMultipleEnvironments::class) { project, buildDir ->
            val customFilename = "${UUID.randomUUID()}-application.properties"

            shouldThrow<IllegalArgumentException> {
                GenerateApplicationProperties.invoke(
                    project,
                    DEFAULT_ENVIRONMENTS,
                    buildDir.absolutePath,
                    customFilename
                )
            }.message shouldBe """
                Received Property to override filename (-P${Configuration::filenameOverride.name}): $customFilename
                However, this can only be used when a single environment is requested/generated. (Tip: Use -P${Configuration::environments.name} to specify environment application properties to generate)
            """.trimIndent()
        }
    }

    @Test
    fun shouldAllowApplicationPropertiesFilenameOverrideWhenASingularEnvironmentApplicationPropertiesHasBeenGenerated() {
        setupScenario(WithSingularEnvironment::class) { project, buildDir ->
            val customFilename = "${UUID.randomUUID()}-application.properties"

            GenerateApplicationProperties.invoke(
                project,
                DEFAULT_ENVIRONMENTS,
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
        propertiesClass: KClass<out Any>,
        callback: (Project, File) -> Unit
    ) {
        mockkStatic(ImplementationClassFinder::class) {
            val buildDir = Files
                .createTempDirectory(DEFAULT_BUILD_DESTINATION)
                .toFile().apply { deleteOnExit() }

            val project = mockk<Project>() {
                every<String> {
                    layout.buildDirectory.dir(buildDir.absolutePath).get().asFile.absolutePath
                } returns buildDir.absolutePath
            }

            every {
                ImplementationClassFinder.findImplementationClass(any())
            } returns propertiesClass

            callback.invoke(project, buildDir)
        }
    }
}
