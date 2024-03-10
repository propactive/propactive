package io.github.propactive.task.support

import io.github.propactive.task.support.PropertyClassLoader.load
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.paths.shouldBeAFile
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import java.net.URLClassLoader
import java.nio.file.Files
import kotlin.reflect.KClass

@TestInstance(PER_CLASS)
class PropertyClassLoaderTest {
    @Test
    fun `should fail if non-directory file was given as a receiver`() {
        val notADirectory = Files
            .createTempFile("test", ".class")
            .apply { shouldBeAFile() }
            .toFile()

        shouldThrow<IllegalStateException> {
            setOf(notADirectory).load("test")
        }.message shouldBe "Non-directory file encountered: $notADirectory"
    }

    @Test
    fun `should find class by given directory url via thread context class loader`() {
        mockkStatic(URLClassLoader::class) {
            val existingClassName = KClass::class.simpleName!!

            val urlClassLoader: URLClassLoader = mockk {
                every { loadClass(existingClassName) } returns KClass::class.java
            }

            val file = Files
                .createTempFile(existingClassName, ".class")
                .apply { shouldBeAFile() }
                .toFile()

            every {
                URLClassLoader.newInstance(
                    listOf(file.parentFile.toURI().toURL()).toTypedArray(),
                    Thread.currentThread().getContextClassLoader(),
                )
            } returns urlClassLoader

            setOf(file.parentFile)
                .load(existingClassName)
                .shouldBe(KClass::class)

            verify {
                URLClassLoader.newInstance(
                    listOf(file.parentFile.toURI().toURL()).toTypedArray(),
                    Thread.currentThread().getContextClassLoader(),
                )
            }
        }
    }
}
