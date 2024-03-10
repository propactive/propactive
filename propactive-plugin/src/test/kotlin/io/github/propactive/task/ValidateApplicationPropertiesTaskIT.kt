package io.github.propactive.task

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
import io.github.propactive.support.extension.project.ProjectDirectory
import io.github.propactive.type.INTEGER
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import org.junit.jupiter.api.TestMethodOrder
import org.junit.jupiter.api.extension.ExtendWith

@TestInstance(PER_CLASS)
@TestMethodOrder(OrderAnnotation::class)
@ExtendWith(PublishSnapshotJars::class, KotlinEnvironmentExtension::class)
class ValidateApplicationPropertiesTaskIT {
    private val taskUnderTest: String = ValidateApplicationPropertiesTask.TASK_NAME

    @Test
    @Order(1)
    fun `should succeed validation when a valid ApplicationProperties is given`(
        taskExecutor: TaskExecutor,
    ) {
        /** First run on a clean [ProjectDirectory] should be SUCCESS */
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
        buildScript.asKts(
            classCompileDependency = "compileKotlin",
        )

        /** Second run with optimised classCompileDependency should re-compile the files (i.e. SUCCESS) */
        taskExecutor
            .execute(taskUnderTest)
            .outcome shouldBe Outcome.SUCCESS

        /** Third run without file modification should not re-compile the files (i.e. UP_TO_DATE) */
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
        val propertyKeyForAnInvalidValue = "propactive.dev.property.with.an.invalid.value.key"

        /** Now we modify the application properties class */
        mainSourceSet.withKotlinFile(APPLICATION_PROPERTIES_CLASS_NAME.plus(".kt")) {
            applicationPropertiesKotlinSource(
                extraEntries = listOf(
                    /** We are setting the [Property] value to a string, which is not a valid [INTEGER] value */
                    "@${Property::class.simpleName}([\"NOT_AN_INT\"], type = ${INTEGER::class.simpleName}::class)",
                    "const val aPropertyWithAnInvalidValueKey = \"$propertyKeyForAnInvalidValue\"",
                ),
            )
        }

        taskExecutor
            .expectFailure()
            .execute(taskUnderTest)
            .apply {
                outcome shouldBe Outcome.FAILED
                output shouldContain "Property named: $propertyKeyForAnInvalidValue was expected to be of type: ${INTEGER::class.simpleName}, but value was: NOT_AN_INT"
            }
    }
}
