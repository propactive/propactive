package io.github.propactive.task

import io.github.propactive.environment.EnvironmentFactory
import io.github.propactive.environment.EnvironmentModel
import io.github.propactive.project.PropertiesFileWriter.writePropertiesFile
import io.github.propactive.plugin.Configuration
import io.github.propactive.plugin.Configuration.Companion.DEFAULT_ENVIRONMENTS
import io.github.propactive.project.ImplementationClassFinder
import org.gradle.api.Project

object GenerateApplicationProperties {
    @JvmStatic
    internal fun invoke(project: Project) = project
        .let(ImplementationClassFinder::find)
        .let(EnvironmentFactory::create)
        .let { environmentModels ->
            with(project.extensions.findByType(Configuration::class.java)!!) {
                environmentModels
                    .requireSingleEnvironmentWhenCustomFilenameIsGiven(environments, filenameOverride)
                    .filter { environments.contains(it.name) || environments.contains(DEFAULT_ENVIRONMENTS) }
                    .forEach { environment -> writePropertiesFile(environment, destination, filenameOverride) }
            }
        }

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
