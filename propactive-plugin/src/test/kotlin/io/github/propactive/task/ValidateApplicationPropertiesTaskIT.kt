package io.github.propactive.task

import io.github.propactive.plugin.Configuration
import io.github.propactive.plugin.Propactive.Companion.PROPACTIVE_GROUP
import io.github.propactive.support.extension.gradle.TaskExecutor
import io.github.propactive.support.extension.gradle.TaskExecutor.Outcome
import io.github.propactive.support.extension.project.BuildOutput
import io.github.propactive.support.extension.project.BuildScript
import io.github.propactive.support.extension.project.MainSourceSet
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import java.io.File

class ValidateApplicationPropertiesTaskIT : ApplicationPropertiesTaskIT(
    ValidateApplicationPropertiesTask.TASK_NAME,
) {
    @Test
    @Order(1)
    fun `should succeed validation when a valid ApplicationProperties is given`(
        taskExecutor: TaskExecutor,
    ) {
        taskExecutor
            .execute(taskUnderTest)
            .outcome shouldBe Outcome.SUCCESS
    }

    @Test
    @Order(2)
    fun `should use cached output when task is ran and no sourcecode changes occurred when an explicit class compile dependency with matching source set`(
        taskExecutor: TaskExecutor,
        buildScript: BuildScript,
        buildOutput: BuildOutput,
    ) {
        buildScript.apply {
            val content = readText()
            val newContent = content.replace("$PROPACTIVE_GROUP {", "$PROPACTIVE_GROUP { ${Configuration::classCompileDependency.name} = \"compileKotlin\"")
            writeText(newContent)
        }

        taskExecutor
            .execute(taskUnderTest)
            .outcome shouldBe Outcome.SUCCESS

        taskExecutor
            .execute(taskUnderTest)
            .outcome shouldBe Outcome.UP_TO_DATE
    }

    @Test
    @Order(3)
    fun `should fail validation when an invalid ApplicationProperties is given`(
        taskExecutor: TaskExecutor,
        mainSourceSet: MainSourceSet,
    ) {
        val applicationPropertiesFile = File(mainSourceSet, "ApplicationProperties.kt")

        // i.e. invalidate the cache
        applicationPropertiesFile
            .readText()
            .replace("@Property([\"42\"], type = INTEGER::class)", "@Property([\"NOT_AN_INT\"], type = INTEGER::class)")
            .also(applicationPropertiesFile::writeText)

        taskExecutor
            .expectFailure()
            .execute(taskUnderTest)
            .apply {
                outcome shouldBe Outcome.FAILED
                output shouldContain "Property named: propactive.dev.int.property.key was expected to be of type: INTEGER, but value was: NOT_AN_INT"
            }
    }
}
