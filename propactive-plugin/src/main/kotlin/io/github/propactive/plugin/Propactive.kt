package io.github.propactive.plugin

import io.github.propactive.task.GenerateApplicationProperties
import io.github.propactive.task.GenerateApplicationProperties.DEFAULT_BUILD_DESTINATION
import io.github.propactive.task.GenerateApplicationProperties.DEFAULT_ENVIRONMENTS
import io.github.propactive.task.GenerateApplicationProperties.DEFAULT_FILENAME_OVERRIDE
import io.github.propactive.task.GenerateApplicationProperties.DEFAULT_IMPLEMENTATION_CLASS
import io.github.propactive.task.GenerateApplicationPropertiesTask
import org.gradle.api.Plugin
import org.gradle.api.Project

open class Propactive : Plugin<Project> {
    override fun apply(target: Project) {
        target
            .extensions
            .create(Propactive::class.simpleName!!.lowercase(), Configuration::class.java)

        target
            .tasks
            .register(
                GenerateApplicationProperties.TASK_NAME,
                GenerateApplicationPropertiesTask::class.java
            ) { task ->
                target
                    .extensions
                    .findByType(Configuration::class.java)
                    ?.apply {
                        with(target) {
                            task.environments = propertyOrDefault(
                                Configuration::environments.name,
                                (environments ?: DEFAULT_ENVIRONMENTS)
                            )

                            task.implementationClass = propertyOrDefault(
                                Configuration::implementationClass.name,
                                (implementationClass ?: DEFAULT_IMPLEMENTATION_CLASS)
                            )

                            task.destination = propertyOrDefault(
                                Configuration::destination.name,
                                (destination ?: DEFAULT_BUILD_DESTINATION)
                            )

                            task.filenameOverride = propertyOrDefault(
                                Configuration::filenameOverride.name,
                                (filenameOverride ?: DEFAULT_FILENAME_OVERRIDE)
                            )
                        }
                    }

                task.dependsOn(DEFAULT_BUILD_TASK)
            }
    }

    companion object {
        private const val DEFAULT_BUILD_TASK = "build"
        private fun Project.propertyOrDefault(propertyName: String, default: String) =
            default.takeUnless { hasProperty(propertyName) } ?: "${property(propertyName)}"
    }
}
