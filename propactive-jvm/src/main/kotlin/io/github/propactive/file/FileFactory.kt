package io.github.propactive.file

import io.github.propactive.environment.EnvironmentModel
import java.io.File
import java.nio.file.Path

object FileFactory {
    fun create(environment: EnvironmentModel, destination: String) =
        File(Path.of(destination, environment.filename).toUri())
            .apply { parentFile.mkdirs() }
            .also { file ->
                environment
                    .properties
                    .joinToString(separator = "\n") { "${it.name}=${it.value}" }
                    .apply(file::writeText)
            }
}