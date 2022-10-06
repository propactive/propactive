package io.github.propactive.task

import io.github.propactive.environment.Environment
import io.github.propactive.environment.EnvironmentFactory
import io.github.propactive.project.ImplementationClassFinder
import io.github.propactive.property.Property
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import org.gradle.api.Project
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class ValidateApplicationPropertiesTest {

    @Test
    fun shouldInvokeEnvironmentFactoryToTriggerValidationOfProperties() {
        mockkStatic(ImplementationClassFinder::class, EnvironmentFactory::class) {
            val project = mockk<Project>(relaxed = true)

            every {
                ImplementationClassFinder.findImplementationClass(project)
            } returns AnyEnvironment::class

            ValidateApplicationProperties.invoke(project)

            verify {
                ImplementationClassFinder.findImplementationClass(project)
                EnvironmentFactory.create(AnyEnvironment::class)
            }
        }
    }

    @Test
    fun shouldErrorWhenEnvironmentFactoryFails() {
        mockkStatic(ImplementationClassFinder::class, EnvironmentFactory::class) {
            val kClass = AnyEnvironment::class
            val project = mockk<Project>(relaxed = true)

            every {
                ImplementationClassFinder.findImplementationClass(project)
            } returns kClass

            every {
                EnvironmentFactory.create(kClass)
            } throws Exception("An error...")

            assertThrows<Exception> {
                ValidateApplicationProperties.invoke(project)
            }
        }
    }

    @Environment
    object AnyEnvironment {
        @Property(["envValue"])
        const val property = "test.resource.value"
    }
}
