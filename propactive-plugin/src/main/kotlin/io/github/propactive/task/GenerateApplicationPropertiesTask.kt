package io.github.propactive.task

import io.github.propactive.plugin.Configuration
import io.github.propactive.plugin.Propactive
import io.github.propactive.task.GenerateApplicationProperties.invoke
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

open class GenerateApplicationPropertiesTask : DefaultTask() {
    @get:Input
    internal lateinit var environments: String

    @get:Input
    internal lateinit var implementationClass: String

    @get:Input
    internal lateinit var destination: String

    @get:Input
    internal lateinit var filenameOverride: String

    @TaskAction
    fun run() = project
        .logConfigurationsValues()
        .run { invoke(project, environments, implementationClass, destination, filenameOverride) }

    init {
        group = Propactive::class.simpleName!!.lowercase()
        description = GenerateApplicationProperties.TASK_DESCRIPTION
    }

    private fun Project.logConfigurationsValues(): Project = this.also { project ->
        project.logger.debug(
            """
            |
            | Propactive ${GenerateApplicationProperties.TASK_NAME} - Received the following configurations:
            |  - ${Configuration::environments.name} = $environments 
            |  - ${Configuration::implementationClass.name} = $implementationClass
            |  - ${Configuration::destination.name} = $destination
            |  - ${Configuration::filenameOverride.name} = $filenameOverride
            """.trimMargin()
        )
    }
}
