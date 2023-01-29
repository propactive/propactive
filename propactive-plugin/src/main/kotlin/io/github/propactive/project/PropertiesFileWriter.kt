package io.github.propactive.project

import io.github.propactive.environment.EnvironmentModel
import io.github.propactive.logging.PropactiveLogger.debug
import io.github.propactive.logging.PropactiveLogger.trace
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
            .debug { "Writing properties to file location: $absolutePath" }
            .also { file ->
                environment
                    .debug { "For the following environment: ${ name.takeUnless(String::isBlank) ?: "(Unspecified Environment Name)"}" }
                    .properties
                    .trace { "With the following properties: $this" }
                    .joinToString(separator = "\n") { "${it.name}=${it.value}" }
                    .apply(file::writeText)
                    .trace { "Wrote the following properties to file:\n${file.readText()}" }
            }
    }
}
