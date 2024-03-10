package io.github.propactive.task.support

import io.github.propactive.environment.Environment
import io.github.propactive.plugin.Propactive.Companion.LOGGER
import io.github.propactive.task.ApplicationPropertiesTask
import java.io.File
import java.net.URI
import java.net.URLClassLoader
import kotlin.reflect.KClass

object PropertyClassLoader {
    /**
     * Utility method for loading classes annotated with [Environment] from compiled source directories.
     *
     * It efficiently processes file paths into URLs using a sequence for lazy evaluation, enhancing performance
     * for large collections by handling elements sequentially as needed, instead of all at once.
     *
     * Importantly, this loader operates on directories, not individual class files, to resolve classes according
     * to their package structure. It assumes these directories are at the beginning of the classpath, which is
     * essential for successful class resolution.
     *
     * @receiver [Set] representing [ApplicationPropertiesTask.compiledClassesDirectories] from which to load the class.
     * @param clazz The fully qualified name of the class to load.
     * @return The [KClass] of the specified class.
     * @throws IllegalStateException if the class cannot be found in the provided directories.
     */
    internal fun Set<File>.load(clazz: String): KClass<out Any> = this
        .apply { LOGGER.debug("Processing directories: {}", this) }
        .asSequence()
        .onEach { check(it.isFile.not()) { "Non-directory file encountered: $it" } }
        .map(File::toURI)
        .map(URI::toURL)
        .toList()
        .toTypedArray()
        .apply { LOGGER.debug("Attempting to load class: {} from URLs: {}", clazz, this) }
        .let { urls -> URLClassLoader.newInstance(urls, Thread.currentThread().getContextClassLoader()) }
        .runCatching { loadClass(clazz).kotlin }
        .getOrNull() ?: error("Class not found: $clazz")
}
