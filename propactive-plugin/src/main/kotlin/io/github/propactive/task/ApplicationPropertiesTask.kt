package io.github.propactive.task

import io.github.propactive.environment.EnvironmentFactory
import io.github.propactive.plugin.Configuration
import io.github.propactive.plugin.Configuration.Companion.DEFAULT_CLASS_COMPILE_DEPENDENCY
import io.github.propactive.plugin.Configuration.Companion.DEFAULT_CONFIGURATION
import io.github.propactive.plugin.Propactive
import io.github.propactive.plugin.Propactive.Companion.LOGGER
import org.gradle.api.DefaultTask
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity.RELATIVE
import java.io.File
import java.net.URI
import java.net.URLClassLoader
import kotlin.reflect.KClass

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
    val compiledClasses: FileCollection =
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

    companion object {
        /**
         * Loads a class by its fully qualified name from a collection of files.
         *
         * This method utilizes a sequence for efficient processing of file paths, transforming
         * them into URLs suitable for class loading. A sequence is used here to lazily evaluate
         * the collection transformations, optimizing performance especially for large collections
         * by processing elements one at a time as needed, rather than all at once.
         *
         * The class loader requires directories (not individual class files) to properly resolve
         * class names based on their package structure. Thus, this method ensures that each file
         * in the collection points to a directory. If a file in the collection is not a directory
         * (implying it is likely a class file), its parent directory is used instead. This adjustment
         * is crucial because the URLClassLoader expects to be pointed at the root of the classpath
         * (e.g., a directory where package hierarchies start) to correctly find and load classes.
         * By navigating to the parent directory of class files, we align with how the class loader
         * navigates and interprets the classpath.
         *
         * @param clazz The fully qualified name of the class to load.
         * @return The Kotlin class ([KClass]) corresponding to the specified class name.
         * @throws IllegalStateException if the specified class cannot be found in the provided files.
         */
        @JvmStatic
        internal fun FileCollection.load(clazz: String): KClass<out Any> = this
            .asSequence()
            .map { file -> if (file.isDirectory) file else file.parentFile }
            .distinct()
            .map(File::toURI)
            .map(URI::toURL)
            .toList()
            .toTypedArray()
            .apply { LOGGER.debug("Trying to load class: {} from: {}", clazz, this) }
            .let { urls -> URLClassLoader.newInstance(urls, EnvironmentFactory::class.java.classLoader) }
            .runCatching { loadClass(clazz).kotlin }
            .getOrNull()
            ?: error("Expected to find class: $clazz")
    }
}
