package io.github.propactive.task

import io.github.propactive.environment.EnvironmentFactory
import io.github.propactive.support.utils.alphaNumeric
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.file.shouldNotBeADirectory
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldHaveAnnotation
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity.RELATIVE
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import java.io.File
import java.net.URLClassLoader
import kotlin.random.Random
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.full.memberProperties

class ApplicationPropertiesTaskTest {

    @Nested
    inner class Cache {
        @Test
        fun `should mark task as cacheable`() {
            ApplicationPropertiesTask::class.java
                .shouldHaveAnnotation(CacheableTask::class.java)
        }

        @Test
        fun `should mark configuration environments as stable input`() {
            ApplicationPropertiesTask::environments
                .shouldHaveAnnotation(Input::class)
        }

        @Test
        fun `should mark configuration implementationClass as stable input`() {
            ApplicationPropertiesTask::implementationClass
                .shouldHaveAnnotation(Input::class)
        }

        @Test
        fun `should mark configuration filenameOverride as stable input`() {
            ApplicationPropertiesTask::filenameOverride
                .shouldHaveAnnotation(Input::class)
        }

        @Test
        fun `should mark configuration compiledClasses as stable inputFiles with relative path`() {
            ApplicationPropertiesTask::compiledClasses
                .apply { shouldHaveAnnotation(InputFiles::class) }
                .shouldHaveAnnotation(PathSensitive::class)
                .let { it as PathSensitive }
                .value shouldBe RELATIVE
        }

        @Test
        fun `should mark configuration destination as stable output directory`() {
            ApplicationPropertiesTask::destination
                .shouldHaveAnnotation(OutputDirectory::class)
        }
    }

    @Nested
    inner class Load {
        private val existingClassName = KClass::class.simpleName!!
        private val givenPathname = Random.alphaNumeric("path/to/your/desired/location/ApplicationProperties", ".class")

        private lateinit var file: File
        private lateinit var urlClassLoader: URLClassLoader
        private lateinit var fileCollection: FileCollection

        @BeforeEach
        fun setUp() {
            file = File(givenPathname)
            urlClassLoader = mockk { every { loadClass(existingClassName) } returns KClass::class.java }
            fileCollection = mockk { every { iterator() } returns listOf(file).toMutableList().listIterator() }

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

            ApplicationPropertiesTask.apply {
                fileCollection
                    .load(existingClassName)
                    .shouldBe(KClass::class)
            }

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

            ApplicationPropertiesTask.apply {
                shouldThrow<IllegalStateException> {
                    fileCollection.load(clazz)
                }.message shouldBe "Expected to find class: $clazz"
            }
        }
    }

    private fun <V> KProperty<V>.shouldHaveAnnotation(annotation: KClass<out Any>) =
        ApplicationPropertiesTask::class.memberProperties
            .first { it.name == this.name }
            .getter
            .annotations
            .find { it.annotationClass == annotation }
            ?: fail { "Expected to find '$annotation' annotation on '${this.name}' property" }
}
