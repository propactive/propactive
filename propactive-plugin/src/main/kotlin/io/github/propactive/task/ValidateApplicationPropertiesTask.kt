package io.github.propactive.task

import io.github.propactive.environment.EnvironmentFactory
import io.github.propactive.plugin.Configuration
import io.github.propactive.plugin.Configuration.Companion.DEFAULT_IMPLEMENTATION_CLASS
import io.github.propactive.plugin.Propactive.Companion.LOGGER
import io.github.propactive.plugin.Propactive.Companion.PROPACTIVE_GROUP
import io.github.propactive.task.support.PropertyClassLoader.load
import org.gradle.api.tasks.TaskAction

open class ValidateApplicationPropertiesTask : ApplicationPropertiesTask() {
    companion object {
        internal val TASK_NAME =
            ValidateApplicationPropertiesTask::class.simpleName!!
                .replaceFirstChar(Char::lowercaseChar)
                .removeSuffix("Task")

        internal val TASK_DESCRIPTION = """
            |Validates the application properties without generating any files.
            |
            |  Optional configurations:
            |    -P${Configuration::implementationClass.name}
            |        Description: Sets the location of your properties object.
            |        Example: "com.package.path.to.your.ApplicationProperties"
            |        Default: "$DEFAULT_IMPLEMENTATION_CLASS" (At the root of your project, without a package path.)
            |
        """.trimMargin()
    }

    @TaskAction
    fun run() = compiledClassesDirectories
        .apply { LOGGER.info("Validating application properties") }
        .apply { LOGGER.debug("Task received the following compiled classes directories: {}", this) }
        .load(implementationClass)
        .apply { LOGGER.info("Found Propactive implementation class: {}", this.qualifiedName) }
        .run(EnvironmentFactory::create)
        .apply { LOGGER.info("Success - validated application properties with no errors!") }

    init {
        group = PROPACTIVE_GROUP
        description = TASK_DESCRIPTION
    }
}
