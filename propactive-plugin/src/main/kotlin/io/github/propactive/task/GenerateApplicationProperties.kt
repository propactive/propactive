package io.github.propactive.task

import io.github.propactive.environment.EnvironmentFactory
import io.github.propactive.environment.EnvironmentModel
import io.github.propactive.file.PropertiesFileWriter.writePropertiesFile
import io.github.propactive.plugin.Configuration
import io.github.propactive.project.ImplementationClassFinder.findImplementationClass
import org.gradle.api.Project

object GenerateApplicationProperties {
    internal const val DEFAULT_ENVIRONMENTS = "*"
    internal const val DEFAULT_IMPLEMENTATION_CLASS = "ApplicationProperties"
    internal const val DEFAULT_BUILD_DESTINATION = "properties"
    internal const val DEFAULT_FILENAME_OVERRIDE = ""

    internal val TASK_NAME =
        GenerateApplicationProperties::class.simpleName!!.replaceFirstChar(Char::lowercaseChar)
    internal val TASK_DESCRIPTION = """
        | Generates application properties file for each given environment.
        |
        | Optional configurations:
        | -P${Configuration::environments.name}
        |     Description: Comma separated list of environments to generate the properties.
        |     Example: test,stage,prod
        |     Default: $DEFAULT_ENVIRONMENTS (All provided environments)
        | -P${Configuration::implementationClass.name}
        |     Description: Sets the location of your properties object.
        |     Example: com.package.path.to.your.ApplicationProperties
        |     Default: $DEFAULT_IMPLEMENTATION_CLASS (at the root of your project)
        | -P${Configuration::destination.name}
        |     Description: Sets the location of your generated properties file within the build directory.
        |     Example: path/to/your/desired/location
        |     Default: $DEFAULT_BUILD_DESTINATION (i.e. in a directory called "properties" within your build directory)
        | -P${Configuration::filenameOverride.name}
        |     Description: Allows overriding given filename for an environment.
        |     Example: custom-filename-application.properties
        |     Note: This should only be used when generating application properties for a singular environment.
    """.trimMargin()

    @JvmStatic
    internal fun invoke(
        project: Project,
        environments: String,
        destination: String,
        filenameOverride: String?,
    ) = findImplementationClass(project)
        .let(EnvironmentFactory::create)
        .requireSingleEnvironmentWhenCustomFilenameIsGiven(environments, filenameOverride)
        .filter { environments.contains(it.name) || environments.contains(DEFAULT_ENVIRONMENTS) }
        .forEach { environment -> writePropertiesFile(environment, project.layout.buildDirectory.dir(destination).get().asFile.absolutePath, filenameOverride) }

    private fun Set<EnvironmentModel>.requireSingleEnvironmentWhenCustomFilenameIsGiven(
        environments: String,
        filenameOverride: String?
    ): Set<EnvironmentModel> {
        val isNotMultiEnvOrWildCardValue = environments
            .split(",").singleOrNull()?.equals(DEFAULT_ENVIRONMENTS)?.not() ?: false

        require((filenameOverride.isNullOrBlank() || size == 1 || isNotMultiEnvOrWildCardValue)) {
            """
                Received Property to override filename (-P${Configuration::filenameOverride.name}): $filenameOverride
                However, this can only be used when a single environment is requested/generated. (Tip: Use -P${Configuration::environments.name} to specify environment application properties to generate)
            """.trimIndent()
        }

        return this
    }
}
