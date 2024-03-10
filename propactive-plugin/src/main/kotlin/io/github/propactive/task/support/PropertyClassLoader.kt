package io.github.propactive.task.support

import io.github.propactive.environment.Environment
import io.github.propactive.environment.EnvironmentFactory
import io.github.propactive.plugin.Propactive.Companion.LOGGER
import java.io.File
import java.net.URI
import java.net.URLClassLoader
import kotlin.reflect.KClass

/**
 * A utility class for loading classes annotated with [Environment] from a collection of files.
 */
object PropertyClassLoader {
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
    internal fun List<File>.load(clazz: String): KClass<out Any> = this
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
