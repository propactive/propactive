package io.github.propactive.task.validators

import io.github.propactive.logging.PropactiveLogger.debug
import io.github.propactive.plugin.Configuration

object ConfigurationValidator {
    /** Validates that the given [Configuration] contains a valid class compile dependency Gradle task. (i.e. the task exists) */
    internal val ENSURE_GIVEN_CLASS_COMPILE_DEPENDENCY_EXISTS = TaskValidator { project ->
        val id = project
            .extensions
            .findByType(Configuration::class.java)
            ?.classCompileDependency
            ?: Configuration.DEFAULT_CLASS_COMPILE_DEPENDENCY

        project
            .tasks
            .debug { "Ensuring that the given class compile dependency Gradle task exists: $id" }
            .findByName(id)
            .let { requireNotNull(it) { "Given Gradle task for source class compile dependency was not found: $id" } }
            .name
    }
}
