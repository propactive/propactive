package io.github.propactive.task

import io.github.propactive.environment.EnvironmentFactory
import io.github.propactive.project.ImplementationClassFinder
import org.gradle.api.Project

object ValidateApplicationProperties {
    internal val TASK_NAME =
        ValidateApplicationProperties::class.simpleName!!.replaceFirstChar(Char::lowercaseChar)
    internal val TASK_DESCRIPTION = """
        | Validates the application properties object.
    """.trimMargin()

    @JvmStatic
    internal fun invoke(
        project: Project
    ) = ImplementationClassFinder
        .findImplementationClass(project)
        .let(EnvironmentFactory::create)
}
