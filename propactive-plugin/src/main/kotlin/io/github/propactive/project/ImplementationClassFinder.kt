package io.github.propactive.project

import io.github.propactive.environment.EnvironmentFactory
import io.github.propactive.plugin.Configuration
import org.gradle.api.Project
import java.io.File
import java.net.URL
import java.net.URLClassLoader
import kotlin.reflect.KClass

object ImplementationClassFinder {
    internal const val DEFAULT_IMPLEMENTATION_CLASS_DERIVER_DEPENDENCY = "jar"
    internal const val DEFAULT_IMPLEMENTATION_CLASS = "ApplicationProperties"

    @JvmStatic
    internal fun findImplementationClass(project: Project): KClass<out Any> {
        val implementationClass = project
            .extensions
            .findByType(Configuration::class.java)
            ?.implementationClass
            ?: DEFAULT_IMPLEMENTATION_CLASS

        return project
            .getTasksByName(DEFAULT_IMPLEMENTATION_CLASS_DERIVER_DEPENDENCY, true)
            .fold(setOf<File>()) { acc, task -> acc.plus(task.outputs.files.files) }
            .firstNotNullOfOrNull {
                URLClassLoader
                    .newInstance(arrayOf(URL("jar:file:${it.path}!/")), EnvironmentFactory::class.java.classLoader)
                    .runCatching { loadClass(implementationClass).kotlin }
                    .getOrNull()
            } ?: error("Expected to find implementation class $implementationClass")
    }
}
