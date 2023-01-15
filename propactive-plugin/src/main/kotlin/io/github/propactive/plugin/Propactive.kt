package io.github.propactive.plugin

import io.github.propactive.task.GenerateApplicationPropertiesTask
import io.github.propactive.task.ValidateApplicationPropertiesTask
import org.gradle.api.Plugin
import org.gradle.api.Project

open class Propactive : Plugin<Project> {
    override fun apply(target: Project) {
        target
            .extensions
            .create(PROPACTIVE_GROUP, Configuration::class.java)

        target
            .tasks
            .register(
                GenerateApplicationPropertiesTask.TASK_NAME,
                GenerateApplicationPropertiesTask::class.java,
            )

        target
            .tasks
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

        private fun Project.propertyOrDefault(propertyName: String, default: String) =
            default.takeUnless { hasProperty(propertyName) } ?: "${property(propertyName)}"
    }
}
