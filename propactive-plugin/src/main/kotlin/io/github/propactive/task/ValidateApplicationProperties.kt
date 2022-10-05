package io.github.propactive.task

import org.gradle.api.Project

object ValidateApplicationProperties {
    internal val TASK_NAME =
        ValidateApplicationProperties::class.simpleName!!.replaceFirstChar(Char::lowercaseChar)
    internal val TASK_DESCRIPTION = """
        | Validates the application properties object.
    """.trimMargin()

    internal fun invoke(
        project: Project
    ) = null
}
