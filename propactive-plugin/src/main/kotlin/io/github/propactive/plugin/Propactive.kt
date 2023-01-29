package io.github.propactive.plugin

import io.github.propactive.logging.PropactiveLogger.debug
import io.github.propactive.logging.PropactiveLogger.trace
import io.github.propactive.task.GenerateApplicationPropertiesTask
import io.github.propactive.task.ValidateApplicationPropertiesTask
import org.gradle.api.Plugin
import org.gradle.api.Project

open class Propactive : Plugin<Project> {
    override fun apply(target: Project) {
        target
            .extensions
            .debug { "Creating extension: ${Configuration::class.simpleName}" }
            .create(PROPACTIVE_GROUP, Configuration::class.java)

        target
            .tasks
            .debug { "Registering Task: ${GenerateApplicationPropertiesTask.TASK_NAME}" }
            .register(
                GenerateApplicationPropertiesTask.TASK_NAME,
                GenerateApplicationPropertiesTask::class.java,
            )

        target
            .tasks
            .debug { "Registering Task: ${ValidateApplicationPropertiesTask.TASK_NAME}" }
            .register(
                ValidateApplicationPropertiesTask.TASK_NAME,
                ValidateApplicationPropertiesTask::class.java,
            )

        target
            .extensions
            .findByType(Configuration::class.java)
            ?.apply {
                environments = target.propertyOrDefault(
                    Configuration::environments.name, environments,
                )

                implementationClass = target.propertyOrDefault(
                    Configuration::implementationClass.name, implementationClass,
                )

                destination = target.propertyOrDefault(
                    Configuration::destination.name, destination,
                )

                filenameOverride = target.propertyOrDefault(
                    Configuration::filenameOverride.name, filenameOverride,
                )

                classCompileDependency = target.propertyOrDefault(
                    Configuration::classCompileDependency.name, classCompileDependency,
                )
            }
    }

    companion object {
        internal val PROPACTIVE_GROUP = Propactive::class.simpleName!!.lowercase()

        private fun Project.propertyOrDefault(propertyName: String, default: String): String =
            (
                default
                    .takeUnless { hasProperty(propertyName) } ?: "${property(propertyName)}"
                )
                .trace { "Set $propertyName to: $this" }
    }
}
