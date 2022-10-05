package io.github.propactive.task

import io.github.propactive.environment.EnvironmentFactory
import io.github.propactive.project.ImplementationClassFinder
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import org.gradle.api.Project
import org.junit.jupiter.api.Test
import java.io.File
import java.nio.file.Files
import kotlin.reflect.KClass

internal class ValidateApplicationPropertiesTest {

    @Test
    fun shouldInvokeEnvironmentFactoryToTriggerValidationOfProperties() {
        mockkStatic(EnvironmentFactory::class) {
            val project = mockk<Project>(relaxed = true)

            ValidateApplicationProperties.invoke(project)

            verify {
                EnvironmentFactory.create(any())
            }
        }
    }

    private fun setupScenario(
        propertiesClass: KClass<out Any>,
        callback: (Project, File) -> Unit
    ) {
        mockkStatic(ImplementationClassFinder::class) {
            val buildDir = Files
                .createTempDirectory(GenerateApplicationProperties.DEFAULT_BUILD_DESTINATION)
                .toFile().apply { deleteOnExit() }

            val project = mockk<Project>() {
                every<String> {
                    layout.buildDirectory.dir(buildDir.absolutePath).get().asFile.absolutePath
                } returns buildDir.absolutePath
            }

            every {
                ImplementationClassFinder.findImplementationClass(any())
            } returns propertiesClass

            callback.invoke(project, buildDir)
        }
    }
}
