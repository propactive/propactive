package io.github.propactive.plugin

import io.github.propactive.task.GenerateApplicationPropertiesTask
import io.github.propactive.task.ValidateApplicationPropertiesTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.Logging
import kotlin.reflect.KMutableProperty1

open class Propactive : Plugin<Project> {
    override fun apply(target: Project) {
        target
            .extensions
            .apply { LOGGER.debug("Creating extension: {}", Configuration::class.simpleName) }
            .create(PROPACTIVE_GROUP, Configuration::class.java)

        val applicationPropertiesTaskTaskProvider = target
            .tasks
            .apply { LOGGER.debug("Registering Task: {}", GenerateApplicationPropertiesTask.TASK_NAME) }
            .register(GenerateApplicationPropertiesTask.TASK_NAME, GenerateApplicationPropertiesTask::class.java)

        val validateApplicationPropertiesTaskTaskProvider = target
            .tasks
            .apply { LOGGER.debug("Registering Task: {}", ValidateApplicationPropertiesTask.TASK_NAME) }
            .register(ValidateApplicationPropertiesTask.TASK_NAME, ValidateApplicationPropertiesTask::class.java)

        /** After evaluate ensures that the code within the block is executed after the project's configuration phase is complete */
        target.afterEvaluate {
            target
                .extensions
                .apply { LOGGER.debug("Evaluating $PROPACTIVE_GROUP extension") }
                .findByType(Configuration::class.java)
                ?.apply {
                    fun <T> T.debug(property: KMutableProperty1<Configuration, T>) = apply {
                        LOGGER.debug("Setting '{}' with value: {}", property.name, this)
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
                        .also { dependency ->
                            /**
                             * We need to ensure the task depends on the compile task to ensure the application properties
                             * class is loaded before we attempt to generate the application properties file. Therefore,
                             * if the user has not provided a custom task to depend on, we'll default to the compileKotlin,
                             * compileJava, or both tasks.
                             */
                            (
                                dependency.takeUnless(String::isBlank)
                                    ?.let(::listOf)
                                    ?: listOfNotNull(
                                        target.tasks.findByName("compileKotlin"),
                                        target.tasks.findByName("compileJava"),
                                    )
                                )
                                .forEach { task ->
                                    applicationPropertiesTaskTaskProvider.get().dependsOn(task)
                                    validateApplicationPropertiesTaskTaskProvider.get().dependsOn(task)
                                }
                        }

                    /** This configuration does not support setters via the system properties. */
                    autoGenerateApplicationProperties
                        .debug(Configuration::autoGenerateApplicationProperties)
                        .let { isTrue ->
                            if (isTrue) {
                                target.tasks.named("classes") { task ->
                                    task.dependsOn.add(GenerateApplicationPropertiesTask.TASK_NAME)
                                }
                            }
                        }
                }
        }
    }

    companion object {
        internal val LOGGER = Logging.getLogger(Propactive::class.java)

        internal val PROPACTIVE_GROUP = Propactive::class.simpleName!!.lowercase()

        private fun Project.propertyOrDefault(propertyName: String, default: String): String =
            default
                .takeUnless { hasProperty(propertyName) }
                ?: "${property(propertyName)}"
    }
}
