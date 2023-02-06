package io.github.propactive.task

import io.github.propactive.environment.Environment
import io.github.propactive.environment.EnvironmentFactory
import io.github.propactive.property.Property
import io.github.propactive.support.utils.alphaNumeric
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldHaveAnnotation
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
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
import java.net.URL
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
    inner class Find {
        private val existingClassName = WithEmptyEnvironment::class.simpleName!!
        private val givenPathname = Random.alphaNumeric("given-file-path")

        private lateinit var file: File
        private lateinit var urlClassLoader: URLClassLoader
        private lateinit var fileCollection: FileCollection

        @BeforeEach
        fun setUp() {
            file = File(givenPathname)
            urlClassLoader = mockk { every { loadClass(existingClassName) } returns WithEmptyEnvironment::class.java }
            fileCollection = mockk { every { iterator() } returns listOf(file).toMutableList().listIterator() }

            mockkStatic(URLClassLoader::class)

            every {
                URLClassLoader.newInstance(
                    arrayOf(URL("jar:file:${file.path}!/")),
                    EnvironmentFactory::class.java.classLoader,
                )
            } returns urlClassLoader
        }

        @AfterEach
        fun teardown() {
            unmockkStatic(URLClassLoader::class)
        }

        @Test
        fun `should find class by given file pathname`() {
            ApplicationPropertiesTask.apply {
                fileCollection
                    .find(existingClassName)
                    .shouldBe(WithEmptyEnvironment::class)
            }
        }

        @Test
        fun `should throw exception when class not found`() {
            val clazz = Random.alphaNumeric("UnknownClassName")

            ApplicationPropertiesTask.apply {
                shouldThrow<IllegalStateException> {
                    fileCollection.find(clazz)
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

    @Environment
    object WithEmptyEnvironment {
        @Property
        const val property = "test.resource.value"
    }
}
