package io.github.propactive.task

import io.github.propactive.plugin.Configuration
import io.github.propactive.plugin.Configuration.Companion.DEFAULT_ENVIRONMENTS
import io.github.propactive.plugin.Configuration.Companion.DEFAULT_IMPLEMENTATION_CLASS
import io.github.propactive.plugin.Propactive.Companion.PROPACTIVE_GROUP
import io.github.propactive.task.validators.ConfigurationValidator.ENSURE_GIVEN_CLASS_COMPILE_DEPENDENCY_EXISTS
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

open class ValidateApplicationPropertiesTask : DefaultTask() {
    companion object {
        internal val TASK_NAME =
            ValidateApplicationProperties::class.simpleName!!.replaceFirstChar(Char::lowercaseChar)

        internal val TASK_DESCRIPTION = """
            |Validates the application properties without generating any files.
            |
            |  Optional configurations:
            |    -P${Configuration::environments.name}
            |        Description: Comma separated list of environments to generate the properties for.
            |        Example: "test,stage,prod"
            |        Default: "$DEFAULT_ENVIRONMENTS" (All provided environments)
            |    -P${Configuration::implementationClass.name}
            |        Description: Sets the location of your properties object.
            |        Example: "com.package.path.to.your.ApplicationProperties"
            |        Default: "$DEFAULT_IMPLEMENTATION_CLASS" (At the root of your project)
            |
        """.trimMargin()
    }

    @TaskAction
    fun run() = project
        .let(ValidateApplicationProperties::invoke)

    init {
        group = PROPACTIVE_GROUP
        description = TASK_DESCRIPTION

        project
            .let(ENSURE_GIVEN_CLASS_COMPILE_DEPENDENCY_EXISTS::validate)
            .apply { super.dependsOn(this) }
    }
}
