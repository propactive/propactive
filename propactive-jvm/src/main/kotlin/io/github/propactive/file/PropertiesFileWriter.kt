package io.github.propactive.file

import io.github.propactive.environment.EnvironmentModel
import java.io.File
import java.nio.file.Path

object PropertiesFileWriter {
    @JvmStatic
    fun writePropertiesFile(
        environment: EnvironmentModel,
        destination: String,
        filenameOverride: String? = null
    ) {
        val filename = filenameOverride
            .takeUnless { it.isNullOrBlank() } ?: environment.filename

        File(Path.of(destination, filename).toUri())
            .apply { parentFile.mkdirs() }
            .also { file ->
                environment
                    .properties
                    .joinToString(separator = "\n") { "${it.name}=${it.value}" }
                    .apply(file::writeText)
            }
    }
}
