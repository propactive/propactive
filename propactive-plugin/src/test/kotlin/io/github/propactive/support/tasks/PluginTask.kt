package io.github.propactive.support.tasks

import io.github.propactive.task.GenerateApplicationProperties
import io.github.propactive.task.GenerateApplicationPropertiesTask
import io.github.propactive.task.ValidateApplicationProperties
import io.github.propactive.task.ValidateApplicationPropertiesTask
import org.gradle.api.DefaultTask

enum class PluginTask(val taskName: String, val taskReference: Class<out DefaultTask>) {
    GENERATE_TASK(
        GenerateApplicationProperties::class.simpleName!!.replaceFirstChar(Char::lowercaseChar),
        GenerateApplicationPropertiesTask::class.java,
    ),
    VALIDATE_TASK(
        ValidateApplicationProperties::class.simpleName!!.replaceFirstChar(Char::lowercaseChar),
        ValidateApplicationPropertiesTask::class.java,
    ),
}
