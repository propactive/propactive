package io.github.propactive.task

import io.github.propactive.environment.EnvironmentFactory
import io.github.propactive.plugin.Configuration
import io.github.propactive.plugin.Configuration.Companion.DEFAULT_CONFIGURATION
import org.gradle.api.DefaultTask
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity.RELATIVE
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
    val compiledClasses: FileCollection = project.tasks
        .getByName(configuration.classCompileDependency)
        .outputs
        .files

    @get:OutputDirectory
    val destination: String = configuration.destination

    companion object {
        @JvmStatic
        internal fun FileCollection.find(clazz: String): KClass<out Any> = this
            .firstNotNullOfOrNull {
                URLClassLoader
                    .newInstance(arrayOf(URI("jar:file:${it.path}!/").toURL()), EnvironmentFactory::class.java.classLoader)
                    .runCatching { loadClass(clazz).kotlin }
                    .getOrNull()
            } ?: error("Expected to find class: $clazz")
    }
}
