package io.github.propactive.task

import io.github.propactive.support.extension.gradle.TaskExecutor
import io.github.propactive.support.extension.gradle.TaskExecutor.Outcome
import io.github.propactive.support.extension.project.BuildOutput
import io.github.propactive.support.extension.project.BuildScript
import io.github.propactive.support.extension.project.MainSourceSet
import io.kotest.matchers.file.shouldExist
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import java.io.File

class GenerateApplicationPropertiesTaskIT : ApplicationPropertiesTaskIT(
    GenerateApplicationPropertiesTask.TASK_NAME,
) {
    @Test
    @Order(1)
    fun `should be able to generate a new property file when on first run`(
        taskExecutor: TaskExecutor,
        buildOutput: BuildOutput,
    ) {
        taskExecutor
            .execute(taskUnderTest)
            .outcome shouldBe Outcome.SUCCESS

        File(buildOutput, "properties/application.properties").shouldExist()
    }

    @Test
    @Order(2)
    fun `should use cached output when task is ran and no sourcecode changes occurred`(
        taskExecutor: TaskExecutor,
        buildOutput: BuildOutput,
    ) {
        taskExecutor
            .execute(taskUnderTest)
            .outcome shouldBe Outcome.UP_TO_DATE

        File(buildOutput, "properties/application.properties").shouldExist()
    }

    @Test
    @Order(3)
    fun `should generate a new property file when the ApplicationProperties object has been modified`(
        taskExecutor: TaskExecutor,
        mainSourceSet: MainSourceSet,
        buildOutput: BuildOutput,
    ) {
        val applicationPropertiesFile = File(mainSourceSet, "ApplicationProperties.kt")

        // i.e. invalidate the cache
        applicationPropertiesFile
            .readText()
            .replace("propactive.dev.string.property.key", "propactive.dev.string.property.key.modified")
            .also(applicationPropertiesFile::writeText)

        taskExecutor
            .execute(taskUnderTest)
            .outcome shouldBe Outcome.SUCCESS

        File(buildOutput, "properties/application.properties").shouldExist()
    }

    @Test
    @Order(4)
    fun `should generate a new property file when the Configuration extension has been modified`(
        taskExecutor: TaskExecutor,
        buildScript: BuildScript,
        buildOutput: BuildOutput,
    ) {
        // i.e. invalidate the cache
        buildScript
            .readText()
            .replace("filenameOverride = \"application.properties\"", "filenameOverride = \"application.properties.modified\"")
            .also(buildScript::writeText)

        taskExecutor
            .execute(taskUnderTest)
            .outcome shouldBe Outcome.SUCCESS

        File(buildOutput, "properties/application.properties.modified").shouldExist()
    }
}
