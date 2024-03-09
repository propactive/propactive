package io.github.propactive.task

import io.github.propactive.plugin.Configuration
import io.github.propactive.plugin.Configuration.Companion.DEFAULT_CLASS_COMPILE_DEPENDENCY
import io.github.propactive.plugin.Configuration.Companion.DEFAULT_CONFIGURATION
import io.github.propactive.plugin.Propactive
import io.github.propactive.plugin.Propactive.Companion.LOGGER
import org.gradle.api.DefaultTask
import org.gradle.api.file.FileTree
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity.RELATIVE

@CacheableTask
abstract class ApplicationPropertiesTask : DefaultTask() {
    private val configuration: Configuration = project.extensions
        .findByType(Configuration::class.java)
        ?: DEFAULT_CONFIGURATION

    @get:Input
    val environments: String = configuration.environments

    @get:Input
    val implementationClass: String = configuration.implementationClass

    @get:Input
    val filenameOverride: String = configuration.filenameOverride

    @get:InputFiles
    @get:PathSensitive(RELATIVE)
    val compiledClasses: FileTree =
        when (configuration.classCompileDependency) {
            DEFAULT_CLASS_COMPILE_DEPENDENCY -> {
                LOGGER.info("Using default ${Configuration::classCompileDependency.name}: $DEFAULT_CLASS_COMPILE_DEPENDENCY")
                LOGGER.info("This will hinder cache optimization. Please provide a specific ${Configuration::classCompileDependency.name} to optimize the task.")
                LOGGER.info("E.g, if you are using Kotlin and your class is within the source set, set ${Configuration::classCompileDependency.name} to: 'compileKotlin'")
                project.layout.buildDirectory.asFileTree
            }
            else -> {
                LOGGER.debug("Using ${Configuration::classCompileDependency.name}: ${configuration.classCompileDependency}")
                project.tasks.getByName(configuration.classCompileDependency).outputs.files.asFileTree
            }
        }.matching { f -> f.include("**/*.class") }

    @get:OutputDirectory
    val destination: String = configuration.destination

    init {
        group = Propactive.PROPACTIVE_GROUP
        dependsOn.add(configuration.classCompileDependency)
    }
}
