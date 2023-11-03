package io.github.propactive.task

import io.github.propactive.support.extension.project.BuildOutput
import io.github.propactive.support.extension.project.MainSourceSet
import io.github.propactive.support.extension.project.ProjectDirectory
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import java.io.File

class ValidateApplicationPropertiesTaskIT : ApplicationPropertiesTaskIT(
    ValidateApplicationPropertiesTask.TASK_NAME,
) {
    @Test
    @Order(1)
    fun `should succeed validation when a valid ApplicationProperties is given`(
        projectDir: ProjectDirectory,
        buildOutput: BuildOutput,
    ) {
        GradleRunner
            .create()
            .withProjectDir(projectDir)
            .withArguments(taskUnderTest)
            .withPluginClasspath()
            .build()
            .task(":$taskUnderTest")
            ?.outcome shouldBe TaskOutcome.SUCCESS
    }

    @Test
    @Order(2)
    fun `should use cached output when task is ran and no sourcecode changes occurred`(
        projectDir: ProjectDirectory,
        buildOutput: BuildOutput,
    ) {
        GradleRunner
            .create()
            .withProjectDir(projectDir)
            .withArguments(taskUnderTest)
            .withPluginClasspath()
            .build()
            .task(":$taskUnderTest")
            ?.outcome shouldBe TaskOutcome.UP_TO_DATE
    }

    @Test
    @Order(3)
    fun `should fail validation when an invalid ApplicationProperties is given`(
        projectDir: ProjectDirectory,
        mainSourceSet: MainSourceSet,
        buildOutput: BuildOutput,
    ) {
        val applicationPropertiesFile = File(mainSourceSet, "ApplicationProperties.kt")

        // i.e. invalidate the cache
        applicationPropertiesFile
            .readText()
            .replace("@Property([\"42\"], type = INTEGER::class)", "@Property([\"NOT_AN_INT\"], type = INTEGER::class)")
            .also(applicationPropertiesFile::writeText)

        GradleRunner
            .create()
            .withProjectDir(projectDir)
            .withArguments(taskUnderTest)
            .withPluginClasspath()
            .buildAndFail()
            .apply {
                task(":$taskUnderTest")?.outcome shouldBe TaskOutcome.FAILED
                output shouldContain "Property named: propactive.dev.int.property.key was expected to be of type: INTEGER, but value was: NOT_AN_INT"
            }
    }
}
