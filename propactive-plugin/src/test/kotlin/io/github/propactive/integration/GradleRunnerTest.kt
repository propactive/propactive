package io.github.propactive.integration

import io.github.propactive.logging.PropactiveLogger.debug
import io.github.propactive.logging.PropactiveLogger.info
import io.github.propactive.support.tasks.PluginTask
import io.github.propactive.support.tasks.PluginTask.GENERATE_TASK
import io.github.propactive.support.tasks.PluginTask.VALIDATE_TASK
import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.gradle.testkit.runner.UnexpectedBuildFailure
import org.gradle.tooling.GradleConnector
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import org.junit.jupiter.api.TestMethodOrder
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import java.io.File
import java.nio.file.Files
import kotlin.text.RegexOption.DOT_MATCHES_ALL

/**
 * NOTE:
 *   - These tests depends on the task "publishToMavenLocal" being run before the tests are executed.
 *   - We are using @BeforeAll and @AfterAll to avoid the overhead of creating and deleting the projectDir for each test
 *   - These integration tests are slow and not run in parallel as of current.
 */
class GradleRunnerTest {
    @Nested
    @TestMethodOrder(OrderAnnotation::class)
    @TestInstance(PER_CLASS)
    inner class Kotlin {
        private lateinit var projectDir: File

        @BeforeAll
        fun setUp() {
            projectDir = Files
                .createTempDirectory("under-test-project-")
                .toFile()
                .info { "Created temporary project directory: $name" }
                .withResource("build.gradle.kts")
                .withResource("settings.gradle.kts")
                .withResource("ApplicationProperties.kt", "src/main/kotlin/ApplicationProperties.kt")
                .withResource("log4j2-test.xml", "src/main/resources/log4j2.xml")

            // NOTE:
            //   We are relying on the fact that this sub-module is 1 level down the root project.
            val propactiveRootProject = System
                .getProperty("user.dir")
                .let(::File)
                .parentFile

            // NOTE:
            //   Publish the jars locally, so we can use them in tests within the test scripts we have:
            //   mavenLocal { url = uri("file://${System.getProperty("user.home")}/.m2/repository") }
            GradleConnector
                .newConnector()
                .forProjectDirectory(propactiveRootProject)
                .connect()
                .use { gradle ->
                    gradle
                        .newBuild()
                        .forTasks("publishToMavenLocal")
                        .setStandardOutput(System.out)
                        .setStandardError(System.err)
                        .run()
                }
        }

        @AfterAll
        fun tearDown() {
            projectDir
                .debug { "Listing project directory files: ${projectDir.name}" }
                .walkTopDown()
                .filterNot { it.absolutePath.contains(".gradle") }
                .forEach { debug { it.absolutePath } }

            projectDir
                .info { "Deleting projectDir: $projectDir" }
                .deleteRecursively()
        }

        @Test
        fun `should be able to run the project with given configurations`() {
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
        fun `should display propactive tasks description`() {
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
                        ${GENERATE_TASK.taskName} - .*?

                        ${VALIDATE_TASK.taskName} - .*?
                    """.trimIndent().toRegex(DOT_MATCHES_ALL)

                    task(":$tasksTask")?.outcome shouldBe TaskOutcome.SUCCESS
                }
        }

        @Order(3)
        @ParameterizedTest
        @EnumSource(PluginTask::class)
        fun `should be able to run propactive's tasks`(task: PluginTask) {
            GradleRunner
                .create()
                .withProjectDir(projectDir)
                .withArguments(task.taskName)
                .withPluginClasspath()
                .build()
                .task(":${task.taskName}")
                ?.outcome shouldBe TaskOutcome.SUCCESS
        }

        @Test
        @Order(4)
        fun `should generate a property file when propactive's GenerateApplicationProperties task is ran`() {
            GradleRunner
                .create()
                .withProjectDir(projectDir)
                .withArguments(GENERATE_TASK.taskName)
                .withPluginClasspath()
                .build().apply {
                    // TODO: The TaskOutcome should be UP_TO_DATE once caching is implemented
                    task(":${GENERATE_TASK.taskName}")?.outcome shouldBe TaskOutcome.SUCCESS
                    File(projectDir, "build/properties/application.properties").exists() shouldBe true
                }
        }

        private fun File.withResource(
            name: String,
            override: String? = null,
        ) = this.apply {
            GradleRunnerTest::class.java
                .classLoader
                .getResource(name)
                .let { requireNotNull(it) { "Resource not found: $name" } }
                .readBytes()
                .also { content ->
                    File(this, override ?: name)
                        .apply { parentFile.mkdirs() }
                        .writeBytes(content)
                }
        }
    }
}
