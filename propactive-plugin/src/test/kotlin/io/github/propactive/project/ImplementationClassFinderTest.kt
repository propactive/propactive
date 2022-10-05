package io.github.propactive.project

import io.github.propactive.environment.Environment
import io.github.propactive.plugin.Configuration
import io.github.propactive.project.ImplementationClassFinder.DEFAULT_IMPLEMENTATION_CLASS
import io.github.propactive.project.ImplementationClassFinder.DEFAULT_IMPLEMENTATION_CLASS_DERIVER_DEPENDENCY
import io.github.propactive.project.ImplementationClassFinder.findImplementationClass
import io.github.propactive.property.Property
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import org.gradle.api.Project
import org.gradle.api.Task
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.net.URLClassLoader

internal class ImplementationClassFinderTest {
    private lateinit var urlClassLoader: URLClassLoader
    private lateinit var project: Project
    private lateinit var configuration: Configuration

    @BeforeEach
    fun setUp() {
        mockkStatic(URLClassLoader::class)

        configuration = mockk(relaxed = true)

        urlClassLoader = mockk() {
            every { loadClass(any()) } returns WithEmptyEnvironment::class.java
            every { URLClassLoader.newInstance(any(), any()) } returns this
        }

        project = mockk() {
            val task = mockk<Task> { every { outputs.files.files } returns setOf(mockk(relaxed = true)) }
            every { extensions.findByType(Configuration::class.java) } returns configuration
            every { getTasksByName(any(), true) } returns setOf(task)
        }
    }

    @Test
    fun shouldUseDefaultTaskForWhenClassImplementationNeedsToBeCollected() {
        findImplementationClass(project)

        verify {
            project.getTasksByName(DEFAULT_IMPLEMENTATION_CLASS_DERIVER_DEPENDENCY, true)
        }
    }

    @Test
    fun shouldUseDefaultImplementationClassNameWhenNoImplementationClassIsProvidedByTheConfiguration() {
        every {
            configuration.implementationClass
        } returns null

        findImplementationClass(project)

        verify {
            urlClassLoader.loadClass(DEFAULT_IMPLEMENTATION_CLASS)
        }
    }

    /** NOTE:
     *   Very important test as using the wrong classloader can prevent
     *   us from accessing annotations and confuse us with unexpected reflection
     *   errors.
     */
    @Test
    fun shouldUseTheSameClassLoaderAsRuntime() {
        findImplementationClass(project)

        verify { URLClassLoader.newInstance(any(), ImplementationClassFinder::class.java.classLoader) }
    }

    @Test
    fun shouldErrorIfNoImplementationClassWasFound() {
        every {
            urlClassLoader.loadClass(any())
        } throws ClassNotFoundException()

        assertThrows<IllegalStateException> {
            findImplementationClass(project)
        }
    }

    @AfterEach
    fun teardown() {
        unmockkStatic(URLClassLoader::class)
    }

    @Environment
    object WithEmptyEnvironment {
        @Property
        const val property = "test.resource.value"
    }
}
