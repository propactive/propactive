package io.github.propactive.task

import io.github.propactive.environment.Environment
import io.github.propactive.property.Property
import io.github.propactive.task.GenerateApplicationPropertiesTask.Companion.DEFAULT_BUILD_DESTINATION
import io.github.propactive.task.GenerateApplicationPropertiesTask.Companion.DEFAULT_IMPLEMENTATION_CLASS
import io.github.propactive.task.GenerateApplicationPropertiesTask.Companion.ENVIRONMENTS_WILDCARD
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import org.gradle.api.Project
import org.gradle.api.Task
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.fail
import java.net.URLClassLoader
import java.nio.file.Files

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
                ENVIRONMENTS_WILDCARD,
                DEFAULT_IMPLEMENTATION_CLASS,
                DEFAULT_BUILD_DESTINATION
            )
        }.message shouldBe "Expected to find implementation class $DEFAULT_IMPLEMENTATION_CLASS"
    }

    @Test
    fun shouldCreatePropertyFilesWhenImplementationClassIsFound() {
        mockkStatic(URLClassLoader::class) {
            val buildDir = Files
                .createTempDirectory("tmp")
                .toFile().apply { deleteOnExit() }

            val task = mockk<Task> {
                every { outputs.files.files } returns setOf(mockk(relaxed = true))
            }

            val project = mockk<Project> {
                every { getTasksByName("jar", true) } returns setOf(task)
            }

            every { URLClassLoader.newInstance(any(), any()) } returns mockk {
                every { loadClass(any()) } returns WithDifferentEnvironmentValues::class.java
            }

            GenerateApplicationProperties.invoke(
                project,
                ENVIRONMENTS_WILDCARD,
                DEFAULT_IMPLEMENTATION_CLASS,
                buildDir.absolutePath
            )

            buildDir.listFiles().apply {
                this?.apply {
                    count() shouldBe 2
                    sortBy { it.name }
                }?.forEachIndexed { index, file ->
                    file.name shouldBe "env$index-application.properties"
                    file?.readLines()?.first() shouldBe "test.resource.value=env${index}Value"
                } ?: fail("Expected to find 2 property files")
            }
        }
    }

    @Environment(["env0: env0-application.properties", "env1: env1-application.properties"])
    object WithDifferentEnvironmentValues {
        @Property(["env0:env0Value", "env1:env1Value"])
        const val property = "test.resource.value"
    }
}