package io.github.propactive.task

import io.github.propactive.environment.EnvironmentFactory
import io.github.propactive.logging.PropactiveLogger.info
import io.github.propactive.project.ImplementationClassFinder
import org.gradle.api.Project

object ValidateApplicationProperties {
    @JvmStatic
    internal fun invoke(
        project: Project
    ) = project
        .info { "Validating application properties..." }
        .let(ImplementationClassFinder::find)
        .also(EnvironmentFactory::create)
}
