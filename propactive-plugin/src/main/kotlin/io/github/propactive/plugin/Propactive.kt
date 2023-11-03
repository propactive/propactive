package io.github.propactive.plugin

import io.github.propactive.task.GenerateApplicationPropertiesTask
import io.github.propactive.task.ValidateApplicationPropertiesTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import kotlin.reflect.KMutableProperty1

open class Propactive : Plugin<Project> {
    override fun apply(target: Project) {
        val logger = target.logger

        target
            .extensions
            .apply { logger.debug("Creating extension: {}", Configuration::class.simpleName) }
            .create(PROPACTIVE_GROUP, Configuration::class.java)

        target
            .tasks
            .apply { logger.debug("Registering Task: {}", GenerateApplicationPropertiesTask.TASK_NAME) }
            .register(GenerateApplicationPropertiesTask.TASK_NAME, GenerateApplicationPropertiesTask::class.java)

        target
            .tasks
            .apply { logger.debug("Registering Task: {}", ValidateApplicationPropertiesTask.TASK_NAME) }
            .register(ValidateApplicationPropertiesTask.TASK_NAME, ValidateApplicationPropertiesTask::class.java)

        target
            .extensions
            .apply { logger.debug("Configuring extension: {}", Configuration::class.simpleName) }
            .findByType(Configuration::class.java)
            ?.apply {
                fun String.debug(property: KMutableProperty1<Configuration, String>) = apply {
                    logger.debug("Configuring '{}' with value: {}", property.name, this)
                }

                environments = target
                    .propertyOrDefault(Configuration::environments.name, environments)
                    .debug(Configuration::environments)

                implementationClass = target
                    .propertyOrDefault(Configuration::implementationClass.name, implementationClass)
                    .debug(Configuration::implementationClass)

                destination = target
                    .propertyOrDefault(Configuration::destination.name, destination)
                    .debug(Configuration::destination)

                filenameOverride = target
                    .propertyOrDefault(Configuration::filenameOverride.name, filenameOverride)
                    .debug(Configuration::filenameOverride)

                classCompileDependency = target
                    .propertyOrDefault(Configuration::classCompileDependency.name, classCompileDependency)
                    .debug(Configuration::classCompileDependency)
            }
    }

    companion object {
        internal val PROPACTIVE_GROUP = Propactive::class.simpleName!!.lowercase()

        private fun Project.propertyOrDefault(propertyName: String, default: String): String =
            default
                .takeUnless { hasProperty(propertyName) }
                ?: "${property(propertyName)}"
    }
}
