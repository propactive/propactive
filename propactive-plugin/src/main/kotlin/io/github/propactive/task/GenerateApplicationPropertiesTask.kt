package io.github.propactive.task

import io.github.propactive.environment.EnvironmentFactory
import io.github.propactive.file.FileFactory
import io.github.propactive.plugin.Configuration
import io.github.propactive.plugin.Configuration.Companion.DEFAULT_ENVIRONMENTS
import io.github.propactive.plugin.Configuration.Companion.DEFAULT_IMPLEMENTATION_CLASS
import io.github.propactive.plugin.Propactive.Companion.LOGGER
import io.github.propactive.plugin.Propactive.Companion.PROPACTIVE_GROUP
import io.github.propactive.task.support.PropertyClassLoader.load
import org.gradle.api.tasks.TaskAction

open class GenerateApplicationPropertiesTask : ApplicationPropertiesTask() {
    companion object {
        internal val TASK_NAME =
            GenerateApplicationPropertiesTask::class.simpleName!!
                .replaceFirstChar(Char::lowercaseChar)
                .removeSuffix("Task")

        internal val TASK_DESCRIPTION = """
            |Generates application properties file for each given environment.
            |
            |  Optional configurations:
            |    -P${Configuration::environments.name}
            |        Description: Comma separated list of environments to generate the properties for.
            |        Example: "test,stage,prod"
            |        Default: "$DEFAULT_ENVIRONMENTS" (All provided environments)
            |    -P${Configuration::implementationClass.name}
            |        Description: Sets the location of your properties object.
            |        Example: "com.package.path.to.your.ApplicationProperties"
            |        Default: "$DEFAULT_IMPLEMENTATION_CLASS" (At the root of your project, without a package path.)
            |    -P${Configuration::destination.name}
            |        Description: Sets the location of your generated properties file within the build directory.
            |        Example: layout.buildDirectory.dir("properties").get().asFile.absolutePath
            |        Default: layout.buildDirectory.dir("resources/main").get().asFile.absolutePath (In the main resources directory)
            |    -P${Configuration::filenameOverride.name}
            |        Description: Allows overriding given filename for when you're generating properties for a single environment.
            |        Example: "dev-application.properties"
            |        Note: This can only be used when generating application properties for a singular environment.
            |
        """.trimMargin()
    }

    @TaskAction
    fun run() = compiledClassesDirectories
        .apply { LOGGER.info("Generating application properties for environments: {}", environments) }
        .apply { LOGGER.debug("Task received the following compiled classes directories: {}", this) }
        .load(implementationClass)
        .apply { LOGGER.info("Found Propactive implementation class: {}", this.qualifiedName) }
        .run(EnvironmentFactory::create)
        .apply { LOGGER.debug("Created environment models: {}", this) }
        .run(FileFactory::create)
        .apply { LOGGER.debug("Created files models: {}", this) }
        .filter { model -> environments.contains(model.environment) || environments.contains(DEFAULT_ENVIRONMENTS) }
        .apply { LOGGER.debug("Filtered files models as per requested environment(s): {}", this) }
        .apply { check(filenameOverride.isBlank() || size == 1) { "You cannot use ${Configuration::filenameOverride.name} when generating multiple property files" } }
        .apply { if (filenameOverride.isNotBlank()) LOGGER.info("Overriding filename to: {}", filenameOverride) }
        .forEach { file -> file.write(destination, filenameOverride) }
        .apply { LOGGER.info("Success - wrote application properties to: {}", destination) }

    init {
        group = PROPACTIVE_GROUP
        description = TASK_DESCRIPTION
    }
}
