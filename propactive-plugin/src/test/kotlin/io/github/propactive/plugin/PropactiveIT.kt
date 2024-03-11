package io.github.propactive.plugin

import io.github.propactive.support.extension.KotlinEnvironmentExtension
import io.github.propactive.support.extension.PublishSnapshotJars
import io.github.propactive.support.extension.gradle.TaskExecutor
import io.github.propactive.support.extension.gradle.TaskExecutor.Outcome
import io.github.propactive.support.extension.project.ProjectDirectory
import io.github.propactive.task.GenerateApplicationPropertiesTask
import io.github.propactive.task.ValidateApplicationPropertiesTask
import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.gradle.testkit.runner.UnexpectedBuildFailure
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import org.junit.jupiter.api.TestMethodOrder
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.text.RegexOption.DOT_MATCHES_ALL

@TestInstance(PER_CLASS)
@TestMethodOrder(OrderAnnotation::class)
@ExtendWith(PublishSnapshotJars::class, KotlinEnvironmentExtension::class)
class PropactiveIT {
    @Test
    @Order(1)
    fun `should be able to run the project with given configurations`(
        taskExecutor: TaskExecutor,
    ) {
        shouldNotThrow<UnexpectedBuildFailure> {
            taskExecutor
                .execute("init")
        }
    }

    @Test
    @Order(2)
    fun `should register propactive tasks`(
        projectDirectory: ProjectDirectory,
        taskExecutor: TaskExecutor,
    ) {
        taskExecutor
            .execute("tasks")
            .apply {
                output shouldContain "Propactive tasks"
                output shouldContain "${GenerateApplicationPropertiesTask.TASK_NAME} - .*?".toRegex(DOT_MATCHES_ALL)
                output shouldContain "${ValidateApplicationPropertiesTask.TASK_NAME} - .*?".toRegex(DOT_MATCHES_ALL)
                outcome shouldBe Outcome.SUCCESS
            }
    }
}
