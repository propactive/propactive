package io.github.propactive.task

import io.github.propactive.plugin.Configuration
import io.github.propactive.task.GenerateApplicationProperties.invoke
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

open class GenerateApplicationPropertiesTask : DefaultTask() {
    @get:Input
    internal var environments = project
        .propertyOrDefault(Configuration::environments.name, ENVIRONMENTS_WILDCARD)

    @get:Input
    internal var implementationClass = project
        .propertyOrDefault(Configuration::implementationClass.name, DEFAULT_IMPLEMENTATION_CLASS)

    @get:Input
    internal var destination = project
        .propertyOrDefault(
            Configuration::destination.name,
            project.layout.buildDirectory.dir(DEFAULT_BUILD_DESTINATION).get().asFile.absolutePath
        )

    @TaskAction
    fun run() = project
        .logConfigurationsValues()
        .run { invoke(project, environments, implementationClass, destination) }

    init {
        group = "propactive"
        description = """
            | Generates application properties file for each given environment.
            |
            | Optional configurations:
            | -P${Configuration::environments.name}
            |     Description: Comma separated list of environments to generate the properties.
            |     Example: test,stage,prod
            |     Default: $ENVIRONMENTS_WILDCARD (All provided environment)
            | -P${Configuration::implementationClass.name}
            |     Description: Sets the location of your properties object.
            |     Example: com.package.path.to.your.ApplicationProperties
            |     Default: $implementationClass (at the root of your project)
            | -P${Configuration::destination.name}
            |     Description: Sets the location of your generated properties file.
            |     Example: path/to/your/desired/location
            |     Default: $destination (in a directory called "dist" within your build directory)
        """.trimMargin()
    }

    companion object {
        internal const val ENVIRONMENTS_WILDCARD = "*"
        internal const val DEFAULT_IMPLEMENTATION_CLASS = "ApplicationProperties"
        internal const val DEFAULT_BUILD_DESTINATION = "dist"
    }

    private fun Project.propertyOrDefault(propertyName: String, default: String) = default
        .takeUnless { hasProperty(propertyName) }
        ?: "${property(propertyName)}"

    private fun Project.logConfigurationsValues(): Project = this.also { project ->
        project.logger.debug(
            """
            |
            | Propactive - Received the following configurations:
            |  - environments        = $environments 
            |  - implementationClass = $implementationClass
            |  - destination         = $destination
            |
            """.trimMargin()
        )
    }
}