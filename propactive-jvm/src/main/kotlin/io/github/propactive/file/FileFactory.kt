package io.github.propactive.file

import io.github.propactive.environment.EnvironmentModel
import io.github.propactive.logging.PropactiveLogger.debug
import io.github.propactive.logging.PropactiveLogger.trace

/**
 * Instead of writing to files, this class creates [FileModel]s from [EnvironmentModel]s.
 * This reduces the amount of I/O operations and allows for easier testing.
 */
object FileFactory {
    /**
     * Creates a [Set] of [FileModel]s from a [Set] of [EnvironmentModel]s.
     *
     * @param environments The [Set] of [EnvironmentModel]s to create [FileModel]s from.
     * @return A [Set] of [FileModel]s.
     */
    @JvmStatic
    fun create(environments: Set<EnvironmentModel>): Set<FileModel> = environments
        .debug { "Creating file models from environments: $this" }
        .map { environment ->
            FileModel(
                environment.name,
                environment.filename,
                environment
                    .properties
                    .joinToString(separator = "\n") { property -> "${property.name}=${property.value}" },
            )
        }
        .trace { "Created file models: $this" }
        .toSet()
}
