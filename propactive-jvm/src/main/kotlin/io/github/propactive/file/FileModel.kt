package io.github.propactive.file

import java.io.File
import java.nio.file.Path

data class FileModel(
    val environment: String,
    val filename: String,
    val content: String,
) {
    override fun hashCode() = filename.hashCode()
    override fun equals(other: Any?) = when {
        this === other -> true
        javaClass != other?.javaClass -> false
        else -> (filename == (other as FileModel).filename)
    }

    fun write(destination: String, filenameOverride: String? = null) {
        val filename = filenameOverride
            ?.takeUnless(String::isNullOrBlank) ?: filename

        File(Path.of(destination, filename).toUri())
            .apply { parentFile.mkdirs() }
            .apply { writeText(content) }
    }
}
