package io.github.propactive.task

import io.github.propactive.plugin.Configuration
import io.github.propactive.plugin.Configuration.Companion.DEFAULT_BUILD_DESTINATION
import io.github.propactive.plugin.Configuration.Companion.DEFAULT_ENVIRONMENTS
import io.github.propactive.plugin.Configuration.Companion.DEFAULT_IMPLEMENTATION_CLASS
import io.github.propactive.plugin.Propactive.Companion.PROPACTIVE_GROUP
import io.github.propactive.project.ImplementationClassFinder.DEFAULT_IMPLEMENTATION_CLASS_DERIVER_DEPENDENCY
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

open class GenerateApplicationPropertiesTask : DefaultTask() {
    companion object {
        internal val TASK_NAME =
            GenerateApplicationProperties::class.simpleName!!.replaceFirstChar(Char::lowercaseChar)

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
            |        Default: "$DEFAULT_IMPLEMENTATION_CLASS" (At the root of your project)
            |    -P${Configuration::destination.name}
            |        Description: Sets the location of your generated properties file within the build directory.
            |        Example: "path/to/your/desired/location"
            |        Default: "$DEFAULT_BUILD_DESTINATION" (i.e. in a directory called "properties" within your build directory)
            |    -P${Configuration::filenameOverride.name}
            |        Description: Allows overriding given filename for an environment.
            |        Example: "custom-filename-application.properties"
            |        Note: This should only be used when generating application properties for a singular environment.
            |
        """.trimMargin()
    }

    @TaskAction
    fun run() = project
        .let(GenerateApplicationProperties::invoke)

    init {
        group = PROPACTIVE_GROUP
        description = TASK_DESCRIPTION

        super.dependsOn(DEFAULT_IMPLEMENTATION_CLASS_DERIVER_DEPENDENCY)
    }
}
