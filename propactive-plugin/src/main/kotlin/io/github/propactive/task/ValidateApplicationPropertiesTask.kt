package io.github.propactive.task

import io.github.propactive.plugin.Configuration
import io.github.propactive.plugin.Propactive.Companion.PROPACTIVE_GROUP
import io.github.propactive.project.ImplementationClassFinder.DEFAULT_IMPLEMENTATION_CLASS_DERIVER_DEPENDENCY
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

open class ValidateApplicationPropertiesTask : DefaultTask() {
    companion object {
        internal val TASK_NAME =
            ValidateApplicationProperties::class.simpleName!!.replaceFirstChar(Char::lowercaseChar)

        internal val TASK_DESCRIPTION = """
            | Validates the application properties without generating any files.
            |
            | Optional configurations:
            | -P${Configuration::environments.name}
            |     Description: Comma separated list of environments to generate the properties.
            |     Example: test,stage,prod
            |     Default: ${Configuration.DEFAULT_ENVIRONMENTS} (All provided environments)
            | -P${Configuration::implementationClass.name}
            |     Description: Sets the location of your properties object.
            |     Example: com.package.path.to.your.ApplicationProperties
            |     Default: ${Configuration.DEFAULT_IMPLEMENTATION_CLASS} (at the root of your project)
            |
        """.trimMargin()
    }

    @TaskAction
    fun run() = project
        .let(ValidateApplicationProperties::invoke)

    init {
        group = PROPACTIVE_GROUP
        description = TASK_DESCRIPTION

        super.dependsOn(DEFAULT_IMPLEMENTATION_CLASS_DERIVER_DEPENDENCY)
    }
}
