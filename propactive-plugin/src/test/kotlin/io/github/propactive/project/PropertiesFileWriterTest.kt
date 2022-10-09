package io.github.propactive.project

import io.github.propactive.environment.EnvironmentModel
import io.github.propactive.project.PropertiesFileWriter.writePropertiesFile
import io.github.propactive.property.PropertyModel
import io.github.propactive.support.utils.alphaNumeric
import io.kotest.matchers.file.shouldExist
import io.kotest.matchers.string.shouldContain
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.io.File
import java.nio.file.Files.createTempDirectory
import java.nio.file.Path
import kotlin.random.Random

internal class PropertiesFileWriterTest {

    @Nested
    inner class HappyPath {
        @Test
        fun shouldCreateFileInGivenDestination() {
            val givenFilename = Random.alphaNumeric()
            val environment = mockk<EnvironmentModel>(relaxed = true) {
                every { filename } returns givenFilename
            }

            createTempDirectory("")
                .toFile()
                .apply { deleteOnExit() }
                .let { destinationDir ->
                    writePropertiesFile(environment, destinationDir.absolutePath)

                    File(Path.of(destinationDir.absolutePath, givenFilename).toUri()).shouldExist()
                }
        }

        @Test
        fun shouldWriteApplicationPropertiesToFileInCorrectFormat() {
            val property1 = mockedProperty(1)
            val property2 = mockedProperty(2)
            val property3 = mockedProperty(3)
            val fileName = Random.alphaNumeric()
            val environment = mockk<EnvironmentModel> {
                every { filename } returns fileName
                every { properties } returns setOf(property1, property2, property3)
            }

            createTempDirectory("")
                .toFile()
                .apply { deleteOnExit() }
                .let { destinationDir ->
                    writePropertiesFile(environment, destinationDir.absolutePath)

                    File(Path.of(destinationDir.absolutePath, fileName).toUri())
                        .readText()
                        .shouldContain(
                            """
                            ${property1.name}=${property1.value}
                            ${property2.name}=${property2.value}
                            ${property3.name}=${property3.value}
                            """.trimIndent()
                        )
                }
        }

        @Test
        fun shouldOverrideFilenameIfCustomFilenameIsProvided() {
            val customFilename = Random.alphaNumeric()
            val environment = mockk<EnvironmentModel>(relaxed = true) {
                every { filename } returns Random.alphaNumeric()
            }

            createTempDirectory("")
                .toFile()
                .apply { deleteOnExit() }
                .let { destinationDir ->
                    writePropertiesFile(environment, destinationDir.absolutePath, customFilename)

                    File(Path.of(destinationDir.absolutePath, customFilename).toUri()).shouldExist()
                }
        }

        @Test
        fun shouldNotOverrideFilenameIfCustomFilenameIsBlank() {
            val givenFilename = Random.alphaNumeric()
            val environment = mockk<EnvironmentModel>(relaxed = true) {
                every { filename } returns givenFilename
            }

            createTempDirectory("")
                .toFile()
                .apply { deleteOnExit() }
                .let { destinationDir ->
                    writePropertiesFile(environment, destinationDir.absolutePath, "")

                    File(Path.of(destinationDir.absolutePath, givenFilename).toUri()).shouldExist()
                }
        }

        private fun mockedProperty(number: Int): PropertyModel = mockk {
            every { name } returns "property.name.$number"
            every { value } returns "property.value.$number"
        }
    }
}
