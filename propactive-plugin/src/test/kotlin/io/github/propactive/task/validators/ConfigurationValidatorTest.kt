package io.github.propactive.task.validators

import io.github.propactive.plugin.Configuration
import io.github.propactive.task.validators.ConfigurationValidator.ENSURE_GIVEN_CLASS_COMPILE_DEPENDENCY_EXISTS
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.gradle.api.Project
import org.gradle.api.Task
import org.junit.jupiter.api.Test

class ConfigurationValidatorTest {
    @Test
    fun `should throw IllegalArgumentException when a given classCompileDependency configuration does not exist`() {
        val nonExistentTask = "nonExistentTask"

        val project = mockk<Project> {
            val configuration = mockk<Configuration> {
                every { classCompileDependency } returns nonExistentTask
            }

            every { extensions.findByType(Configuration::class.java) } returns configuration
            every { tasks.findByName(nonExistentTask) } returns null
        }

        shouldThrow<IllegalArgumentException> {
            ENSURE_GIVEN_CLASS_COMPILE_DEPENDENCY_EXISTS.validate(project)
        }.message shouldBe "Given Gradle task for source class compile dependency was not found: $nonExistentTask"
    }

    @Test
    fun `should return taskName when a given classCompileDependency exists`() {
        val anExistentTask = "anExistentTask"

        val project = mockk<Project> {
            val configuration = mockk<Configuration> {
                every { classCompileDependency } returns anExistentTask
            }

            val task = mockk<Task>() {
                every { name } returns anExistentTask
            }

            every { extensions.findByType(Configuration::class.java) } returns configuration
            every { tasks.findByName(anExistentTask) } returns task
        }

        ENSURE_GIVEN_CLASS_COMPILE_DEPENDENCY_EXISTS.validate(project) shouldBe anExistentTask
    }
}
