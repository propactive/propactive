package io.github.propactive.task

import io.github.propactive.plugin.Configuration.Companion.DEFAULT_CLASS_COMPILE_DEPENDENCY
import io.github.propactive.plugin.Configuration.Companion.DEFAULT_ENVIRONMENTS
import io.github.propactive.plugin.Configuration.Companion.DEFAULT_IMPLEMENTATION_CLASS
import io.github.propactive.plugin.Propactive.Companion.PROPACTIVE_GROUP
import io.github.propactive.task.GenerateApplicationPropertiesTask.Companion.TASK_NAME
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.shouldBe
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ValidateApplicationPropertiesTaskTest {
    private lateinit var task: ValidateApplicationPropertiesTask

    @BeforeEach
    internal fun setUp() {
        task = ProjectBuilder.builder()
            .build()
            .also { p -> p.plugins.apply("java") }
            .tasks
            .create(TASK_NAME, ValidateApplicationPropertiesTask::class.java)
    }

    @Test
    fun `should set task group`() {
        task.group shouldBe PROPACTIVE_GROUP
    }

    @Test
    fun `should set class compile dependency`() {
        task.dependsOn.shouldNotBeEmpty()
        task.dependsOn shouldContain DEFAULT_CLASS_COMPILE_DEPENDENCY
    }

    @Test
    fun `should set environment input`() {
        task.environments shouldBe DEFAULT_ENVIRONMENTS
    }

    @Test
    fun `should set implementation class input`() {
        task.implementationClass shouldBe DEFAULT_IMPLEMENTATION_CLASS
    }
}
