package io.github.propactive.plugin

import io.github.propactive.support.extension.KotlinEnvironmentExtension
import io.github.propactive.support.extension.project.ProjectDirectory
import io.github.propactive.task.GenerateApplicationPropertiesTask
import io.github.propactive.task.ValidateApplicationPropertiesTask
import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.gradle.testkit.runner.UnexpectedBuildFailure
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import org.junit.jupiter.api.TestMethodOrder
import org.junit.jupiter.api.extension.ExtendWith

@TestInstance(PER_CLASS)
@TestMethodOrder(OrderAnnotation::class)
@ExtendWith(KotlinEnvironmentExtension::class)
class PropactiveIT {
    @Test
    @Order(1)
    fun `should be able to run the project with given configurations`(projectDir: ProjectDirectory) {
        shouldNotThrow<UnexpectedBuildFailure> {
            GradleRunner
                .create()
                .withProjectDir(projectDir)
                .withPluginClasspath()
                .forwardOutput()
                .build()
        }
    }

    @Test
    @Order(2)
    fun `should display Propactive's tasks description`(projectDir: ProjectDirectory) {
        val tasksTask = "tasks"

        GradleRunner
            .create()
            .withProjectDir(projectDir)
            .withArguments(tasksTask)
            .withPluginClasspath()
            .build()
            .apply {
                output shouldContain """
                        Propactive tasks
                        ----------------
                        ${GenerateApplicationPropertiesTask.TASK_NAME} - .*?

                        ${ValidateApplicationPropertiesTask.TASK_NAME} - .*?
                """.trimIndent().toRegex(RegexOption.DOT_MATCHES_ALL)

                task(":$tasksTask")?.outcome shouldBe TaskOutcome.SUCCESS
            }
    }
}
