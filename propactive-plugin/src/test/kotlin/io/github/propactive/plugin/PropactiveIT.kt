package io.github.propactive.plugin

import io.github.propactive.support.extension.KotlinEnvironmentExtension
import io.github.propactive.support.extension.gradle.TaskExecutor
import io.github.propactive.support.extension.gradle.TaskExecutor.Outcome
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

@TestInstance(PER_CLASS)
@TestMethodOrder(OrderAnnotation::class)
@ExtendWith(KotlinEnvironmentExtension::class)
class PropactiveIT {
    @Test
    @Order(1)
    fun `should be able to run the project with given configurations`(
        taskExecutor: TaskExecutor,
    ) {
        shouldNotThrow<UnexpectedBuildFailure> {
            taskExecutor
                .execute("tasks")
        }
    }

    @Test
    @Order(2)
    fun `should display Propactive's tasks description`(
        taskExecutor: TaskExecutor,
    ) {
        taskExecutor
            .execute("tasks")
            .apply {
                output shouldContain "Propactive tasks"
                output shouldContain "${GenerateApplicationPropertiesTask.TASK_NAME} - .*?".toRegex(RegexOption.DOT_MATCHES_ALL)
                output shouldContain "${ValidateApplicationPropertiesTask.TASK_NAME} - .*?".toRegex(RegexOption.DOT_MATCHES_ALL)

                outcome shouldBe Outcome.SUCCESS
            }
    }
}
