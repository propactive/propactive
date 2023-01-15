package io.github.propactive.task.validators

import org.gradle.api.Project

typealias TaskName = String

fun interface TaskValidator {
    fun validate(project: Project): TaskName
}
