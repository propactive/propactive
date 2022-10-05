package io.github.propactive.task

import io.github.propactive.plugin.Propactive
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

open class ValidateApplicationPropertiesTask : DefaultTask() {
    @TaskAction
    fun run() = project
        .let(ValidateApplicationProperties::invoke)

    init {
        group = Propactive::class.simpleName!!.lowercase()
        description = ValidateApplicationProperties.TASK_DESCRIPTION
    }
}
