package io.github.propactive.task.support

import io.github.propactive.environment.EnvironmentFactory
import io.github.propactive.support.utils.alphaNumeric
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.file.shouldNotBeADirectory
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import org.gradle.api.file.FileCollection
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File
import java.net.URLClassLoader
import kotlin.random.Random
import kotlin.reflect.KClass

class FileExtKtTest {
    private val existingClassName = KClass::class.simpleName!!
    private val givenPathname = Random.alphaNumeric("path/to/your/desired/location/ApplicationProperties", ".class")

    private lateinit var file: File
    private lateinit var urlClassLoader: URLClassLoader
    private lateinit var fileCollection: List<File>

    @BeforeEach
    fun setUp() {
        file = File(givenPathname)
        urlClassLoader = mockk { every { loadClass(existingClassName) } returns KClass::class.java }
        fileCollection = mockk<FileCollection> { every { iterator() } returns listOf(file).toMutableList().listIterator() }.toList()

        mockkStatic(URLClassLoader::class)

        every {
            URLClassLoader.newInstance(
                listOf(file.parentFile.toURI().toURL()).toTypedArray(),
                EnvironmentFactory::class.java.classLoader,
            )
        } returns urlClassLoader
    }

    @AfterEach
    fun tearDown() {
        unmockkStatic(URLClassLoader::class)
    }

    @Test
    fun `should find class by given file pathname`() {
        file.shouldNotBeADirectory()

        fileCollection
            .load(existingClassName)
            .shouldBe(KClass::class)

        verify {
            URLClassLoader.newInstance(
                listOf(file.parentFile.toURI().toURL()).toTypedArray(),
                any(),
            )
        }
    }

    @Test
    fun `should throw exception when class not found`() {
        val clazz = Random.alphaNumeric("UnknownClassName")

        shouldThrow<IllegalStateException> {
            fileCollection.load(clazz)
        }.message shouldBe "Expected to find class: $clazz"
    }
}
