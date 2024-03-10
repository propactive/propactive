package io.github.propactive.support.utils

import io.github.propactive.support.extension.KotlinEnvironmentExtension
import java.io.File

/**
 * Adds a named file to the directory from the test resources.
 *
 * @param name the name of the file to add
 * @return the file that was added
 * @throws IllegalStateException if the receiver file is not a directory or if the named file could not be created.
 *
 * @see loadResourceAsBytes
 */
internal fun <T : File> T.addFileToDirFromTestResources(name: String) = this.apply {
    check(isDirectory) { "File is not a directory: $this" }
    File(this, name)
        .apply(File::createNewFile)
        .apply { check(isFile) { "Failed to create file: $this" } }
        .writeBytes(loadResourceAsBytes(name))
}

private fun loadResourceAsBytes(name: String) = KotlinEnvironmentExtension::class.java
    .classLoader
    .getResource(name)
    .let { url -> requireNotNull(url) { "Resource not found: $name" } }
    .readBytes()
