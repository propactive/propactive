package io.github.propactive.project

import io.github.propactive.environment.EnvironmentFactory
import io.github.propactive.logging.PropactiveLogger.debug
import io.github.propactive.plugin.Configuration
import io.github.propactive.plugin.Configuration.Companion.DEFAULT_IMPLEMENTATION_CLASS
import io.github.propactive.plugin.Configuration.Companion.DEFAULT_CLASS_COMPILE_DEPENDENCY
import org.gradle.api.Project
import java.io.File
import java.net.URL
import java.net.URLClassLoader
import kotlin.reflect.KClass

object ImplementationClassFinder {
    @JvmStatic
    internal fun find(project: Project): KClass<out Any> {
        val configuration = project
            .extensions
            .findByType(Configuration::class.java)
            ?.debug { "Found: $this" }

        val classCompileDependency = configuration
            ?.classCompileDependency
            ?: DEFAULT_CLASS_COMPILE_DEPENDENCY

        val implementationClass = configuration
            ?.implementationClass
            ?: DEFAULT_IMPLEMENTATION_CLASS

        return project
            .debug { "Looking for implementation class \"$implementationClass\" using classes compiled from task: $classCompileDependency" }
            .getTasksByName(classCompileDependency, true)
            .apply { require(isNotEmpty()) { "Couldn't find task: $classCompileDependency" } }
            .fold(setOf<File>()) { acc, task -> acc.plus(task.outputs.files.files) }
            .firstNotNullOfOrNull {
                URLClassLoader
                    .newInstance(arrayOf(URL("jar:file:${it.path}!/")), EnvironmentFactory::class.java.classLoader)
                    .runCatching { loadClass(implementationClass).kotlin }
                    .getOrNull()
                    ?.debug { "Found: $this" }
            } ?: error("Expected to find implementation class $implementationClass")
    }
}
