package io.github.propactive.task

import io.github.propactive.environment.Environment
import io.github.propactive.property.Property
import io.github.propactive.support.extension.KotlinEnvironmentExtension
import io.github.propactive.support.extension.PublishSnapshotJars
import io.github.propactive.support.extension.gradle.TaskExecutor
import io.github.propactive.support.extension.gradle.TaskExecutor.Outcome
import io.github.propactive.support.extension.project.BuildOutput
import io.github.propactive.support.extension.project.BuildScript
import io.github.propactive.support.extension.project.MainSourceSet
import io.github.propactive.support.extension.project.MainSourceSet.Companion.APPLICATION_PROPERTIES_CLASS_NAME
import io.github.propactive.support.extension.project.MainSourceSet.Companion.applicationPropertiesKotlinSource
import io.kotest.matchers.file.shouldExist
import io.kotest.matchers.file.shouldNotExist
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import org.junit.jupiter.api.TestMethodOrder
import org.junit.jupiter.api.extension.ExtendWith
import java.io.File

@TestInstance(PER_CLASS)
@TestMethodOrder(OrderAnnotation::class)
@ExtendWith(PublishSnapshotJars::class, KotlinEnvironmentExtension::class)
class GenerateApplicationPropertiesTaskIT {
    private val taskUnderTest: String = GenerateApplicationPropertiesTask.TASK_NAME

    @Test
    @Order(1)
    fun `should be able to generate a new property file when first run`(
        taskExecutor: TaskExecutor,
        buildScript: BuildScript,
        buildOutput: BuildOutput,
    ) {
        File(buildOutput, "properties/application.properties").shouldNotExist()

        buildScript.asKts(
            /** Rely on [Environment.value] default application properties value (i.e. `application.properties`) */
            filenameOverride = "",
            /** Coverage to test destination functionality */
            destination = buildOutput.resolve("properties").path,
        )

        /** First run should generate the files (i.e. SUCCESS) */
        taskExecutor
            .execute(taskUnderTest)
            .outcome shouldBe Outcome.SUCCESS

        buildOutput
            .resolve("properties/application.properties")
            .shouldExist()
    }

    @Test
    @Order(2)
    fun `should use cached output when task is ran and no sourcecode changes occurred when an explicit class compile dependency with matching source set`(
        taskExecutor: TaskExecutor,
        buildScript: BuildScript,
        buildOutput: BuildOutput,
    ) {
        buildScript.asKts(
            classCompileDependency = "compileKotlin",
        )

        /** Second run with optimised classCompileDependency should re-compile the files because of new resource about from previous test (i.e. SUCCESS) */
        taskExecutor
            .execute(taskUnderTest)
            .outcome shouldBe Outcome.SUCCESS

        /** Third run without file modification should not re-generate the files (i.e. UP_TO_DATE) */
        taskExecutor
            .execute(taskUnderTest)
            .outcome shouldBe Outcome.UP_TO_DATE

        buildOutput
            .resolve("resources/main/application.properties")
            .shouldExist()
    }

    @Test
    @Order(3)
    fun `should generate a new property file when the ApplicationProperties object has been modified`(
        taskExecutor: TaskExecutor,
        mainSourceSet: MainSourceSet,
        buildOutput: BuildOutput,
    ) {
        /** Now we modify the application properties class */
        mainSourceSet.withKotlinFile(APPLICATION_PROPERTIES_CLASS_NAME.plus(".kt")) {
            applicationPropertiesKotlinSource(
                extraEntries = listOf(
                    "@${Property::class.simpleName}([\"XYZ\"])",
                    "const val secondStringPropertyKey = \"propactive.dev.second.string.property.key\"",
                ),
            )
        }

        /** Fourth run should re-generate the files (i.e. SUCCESS) */
        taskExecutor
            .execute(taskUnderTest)
            .outcome shouldBe Outcome.SUCCESS

        buildOutput
            .resolve("resources/main/application.properties")
            .apply(File::shouldExist)
            .readText()
            .shouldContain("propactive.dev.second.string.property.key=XYZ")
    }

    @Test
    @Order(4)
    fun `should generate a new property file when the Configuration extension has been modified`(
        taskExecutor: TaskExecutor,
        buildScript: BuildScript,
        buildOutput: BuildOutput,
    ) {
        /** Change Propactive configurations */
        buildScript.asKts(
            /** Also servers as coverage for filenameOverride functionality */
            filenameOverride = "test-application.properties",
        )

        /** Fifth run should re-generate the files (i.e. SUCCESS) */
        taskExecutor
            .execute(taskUnderTest)
            .outcome shouldBe Outcome.SUCCESS

        buildOutput
            .resolve("resources/main/test-application.properties")
            .shouldExist()
    }

    @Test
    @Order(5)
    fun `should generate Propactive properties file for a class that is not located at root level`(
        taskExecutor: TaskExecutor,
        mainSourceSet: MainSourceSet,
        buildScript: BuildScript,
        buildOutput: BuildOutput,
    ) {
        mainSourceSet
            /** Clean up the source set, so they won't interfere with the test */
            .apply { listFiles()?.onEach(File::deleteRecursively) }
            /** Create a new properties class that is not located at the root level (i.e. in a sub-package) */
            .withKotlinFile("io/github/propactive/properties/$APPLICATION_PROPERTIES_CLASS_NAME.kt") {
                applicationPropertiesKotlinSource(classPackagePath = "io.github.propactive.properties")
            }

        /** Set the Propactive implementation class to the new properties class created */
        buildScript.asKts(
            implementationClass = "io.github.propactive.properties.$APPLICATION_PROPERTIES_CLASS_NAME",
        )

        /** Fifth run should succeed validation (i.e. SUCCESS) */
        taskExecutor
            .execute(taskUnderTest)
            .outcome shouldBe Outcome.SUCCESS

        buildOutput
            .resolve("resources/main/application.properties")
            .shouldExist()
    }
}
