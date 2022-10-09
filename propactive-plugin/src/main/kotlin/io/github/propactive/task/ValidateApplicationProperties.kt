package io.github.propactive.task

import io.github.propactive.environment.EnvironmentFactory
import io.github.propactive.project.ImplementationClassFinder
import org.gradle.api.Project

object ValidateApplicationProperties {
    @JvmStatic
    internal fun invoke(
        project: Project
    ) = project
        .let(ImplementationClassFinder::find)
        .also(EnvironmentFactory::create)
}
