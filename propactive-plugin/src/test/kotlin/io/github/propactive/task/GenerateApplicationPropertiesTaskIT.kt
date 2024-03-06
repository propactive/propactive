package io.github.propactive.task

import io.github.propactive.support.extension.project.BuildOutput
import io.github.propactive.support.extension.project.BuildScript
import io.github.propactive.support.extension.project.MainSourceSet
import io.github.propactive.support.extension.project.ProjectDirectory
import io.kotest.matchers.file.shouldExist
import io.kotest.matchers.shouldBe
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import java.io.File

class GenerateApplicationPropertiesTaskIT : ApplicationPropertiesTaskIT(
    GenerateApplicationPropertiesTask.TASK_NAME,
) {
    @Test
    @Order(1)
    fun `should be able to generate a new property file when on first run`(
        projectDir: ProjectDirectory,
        buildOutput: BuildOutput,
    ) {
        GradleRunner
            .create()
            .forwardOutput()
            .withProjectDir(projectDir)
            .withArguments(taskUnderTest)
            .withPluginClasspath()
            .build()
            .task(":$taskUnderTest")
            ?.outcome shouldBe TaskOutcome.SUCCESS

        File(buildOutput, "properties/application.properties").shouldExist()
    }

    @Test
    @Order(2)
    fun `should use cached output when task is ran and no sourcecode changes occurred`(
        projectDir: ProjectDirectory,
        buildOutput: BuildOutput,
    ) {
        GradleRunner
            .create()
            .forwardOutput()
            .withProjectDir(projectDir)
            .withArguments(taskUnderTest)
            .withPluginClasspath()
            .build()
            .task(":$taskUnderTest")
            ?.outcome shouldBe TaskOutcome.UP_TO_DATE

        File(buildOutput, "properties/application.properties").shouldExist()
    }

    @Test
    @Order(3)
    fun `should generate a new property file when the ApplicationProperties object has been modified`(
        projectDir: ProjectDirectory,
        mainSourceSet: MainSourceSet,
        buildOutput: BuildOutput,
    ) {
        val applicationPropertiesFile = File(mainSourceSet, "ApplicationProperties.kt")

        // i.e. invalidate the cache
        applicationPropertiesFile
            .readText()
            .replace("propactive.dev.string.property.key", "propactive.dev.string.property.key.modified")
            .also(applicationPropertiesFile::writeText)

        GradleRunner
            .create()
            .forwardOutput()
            .withProjectDir(projectDir)
            .withArguments(taskUnderTest)
            .withPluginClasspath()
            .build()
            .task(":$taskUnderTest")
            ?.outcome shouldBe TaskOutcome.SUCCESS

        File(buildOutput, "properties/application.properties").shouldExist()
    }

    @Test
    @Order(4)
    fun `should generate a new property file when the Configuration extension has been modified`(
        projectDir: ProjectDirectory,
        buildScript: BuildScript,
        buildOutput: BuildOutput,
    ) {
        // i.e. invalidate the cache
        buildScript
            .readText()
            .replace("filenameOverride = \"application.properties\"", "filenameOverride = \"application.properties.modified\"")
            .also(buildScript::writeText)

        GradleRunner
            .create()
            .forwardOutput()
            .withProjectDir(projectDir)
            .withArguments(taskUnderTest)
            .withPluginClasspath()
            .build()
            .task(":$taskUnderTest")
            ?.outcome shouldBe TaskOutcome.SUCCESS

        File(buildOutput, "properties/application.properties.modified").shouldExist()
    }
}
