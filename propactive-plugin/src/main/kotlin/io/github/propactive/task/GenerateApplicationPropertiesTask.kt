package io.github.propactive.task

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

    @TaskAction
    fun run() = project
        .logConfigurationsValues()
        .run { invoke(project, environments, implementationClass, destination) }

    init {
        group = Propactive::class.simpleName!!.lowercase()
        description = GenerateApplicationProperties.TASK_DESCRIPTION
    }

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