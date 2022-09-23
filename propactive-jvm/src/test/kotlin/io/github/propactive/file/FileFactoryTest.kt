package io.github.propactive.file

import io.github.propactive.environment.EnvironmentModel
import io.github.propactive.property.PropertyModel
import io.github.propactive.support.utils.alphaNumeric
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.nio.file.Files
import kotlin.random.Random

internal class FileFactoryTest {

    @Nested
    inner class HappyPath {
        @Test
        fun shouldCreateFileInGivenDestination() {
            val givenFilename = Random.alphaNumeric()
            val environment = mockk<EnvironmentModel>(relaxed = true) {
                every { filename } returns givenFilename
            }

            Files
                .createTempDirectory("")
                .toFile()
                .apply { deleteOnExit() }
                .let { destinationDir ->
                    FileFactory
                        .create(environment, destinationDir.absolutePath)
                        .apply {
                            name shouldBe givenFilename
                            parentFile.absolutePath shouldBe destinationDir.absolutePath
                        }
                }
        }

        @Test
        fun shouldWriteApplicationPropertiesToFileInCorrectFormat() {
            val property1 = mockedProperty(1)
            val property2 = mockedProperty(2)
            val property3 = mockedProperty(3)
            val environment = mockk<EnvironmentModel> {
                every { filename } returns Random.alphaNumeric()
                every { properties } returns setOf(property1, property2, property3)
            }

            Files
                .createTempDirectory("")
                .toFile()
                .apply { deleteOnExit() }
                .let { destinationDir ->
                    FileFactory
                        .create(environment, destinationDir.absolutePath)
                        .readText()
                        .shouldContain("""
                            ${property1.name}=${property1.value}
                            ${property2.name}=${property2.value}
                            ${property3.name}=${property3.value}
                        """.trimIndent())
                }
        }

        @Test
        fun shouldOverrideFilenameIfCustomFilenameIsProvided() {
            val customFilename = Random.alphaNumeric()
            val environment = mockk<EnvironmentModel>(relaxed = true) {
                every { filename } returns Random.alphaNumeric()
            }

            Files
                .createTempDirectory("")
                .toFile()
                .apply { deleteOnExit() }
                .let { destinationDir ->
                    FileFactory
                        .create(environment, destinationDir.absolutePath, customFilename)
                        .name shouldBe customFilename
                }
        }

        @Test
        fun shouldNotOverrideFilenameIfCustomFilenameIsBlank() {
            val givenFilename = Random.alphaNumeric()
            val environment = mockk<EnvironmentModel>(relaxed = true) {
                every { filename } returns givenFilename
            }

            Files
                .createTempDirectory("")
                .toFile()
                .apply { deleteOnExit() }
                .let { destinationDir ->
                    FileFactory
                        .create(environment, destinationDir.absolutePath, "")
                        .name shouldBe givenFilename
                }
        }

        private fun mockedProperty(number: Int): PropertyModel = mockk {
            every { name } returns "property.name.$number"
            every { value } returns "property.value.$number"
        }
    }
}