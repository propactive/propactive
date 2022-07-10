package propactive.plugin

import io.kotest.matchers.string.shouldContain
import io.mockk.mockk
import io.mockk.verify
import org.gradle.api.Project
import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.*
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import propactive.task.GenerateApplicationProperties
import propactive.task.GenerateApplicationPropertiesTask
import java.io.File
import java.nio.file.Files
import kotlin.text.RegexOption.DOT_MATCHES_ALL

class PropactiveTest {
    @Nested
    inner class Unit {
        private lateinit var underTest: Propactive
        private lateinit var target: Project

        @BeforeEach
        internal fun setUp() {
            underTest = Propactive()
            target = mockk(relaxed = true)
            underTest.apply(target)
        }

        @Test
        fun `should register Configuration extension`() {
            verify {
                target
                    .extensions
                    .create(Propactive::class.simpleName!!.lowercase(), Configuration::class.java)
            }
        }

        @Test
        fun `should register GenerateApplicationProperties task`() {
            verify {
                target
                    .tasks
                    .register(
                        GenerateApplicationProperties::class.simpleName!!.replaceFirstChar(Char::lowercaseChar),
                        GenerateApplicationPropertiesTask::class.java,
                        any()
                    )
            }
        }
    }

    @Nested
    @TestInstance(PER_CLASS)
    inner class Integration {
        private lateinit var projectDir: File

        @BeforeEach
        fun setUp() {
            projectDir = Files
                .createTempDirectory("projectDir")
                .toFile()
        }

        @AfterEach
        fun tearDown() {
            projectDir.delete()
        }

        @Test
        fun `should display propactive tasks description`() {
            projectDir.also { parent ->
                File(parent, "build.gradle.kts")
                    .apply {
                        writeText(
                            """ 
                            | plugins {
                            |     id("io.github.propactive") version "DEV-SNAPSHOT"
                            | }
                            """.trimMargin()
                        )
                    }
            }

            GradleRunner
                .create()
                .withProjectDir(projectDir)
                .withArguments("tasks")
                .withPluginClasspath()
                .build()
                .apply {
                    output shouldContain """
                    Propactive tasks
                    ----------------
                    generateApplicationProperties - .*?
                    """.trimIndent().toRegex(DOT_MATCHES_ALL)
                }
        }
    }
}