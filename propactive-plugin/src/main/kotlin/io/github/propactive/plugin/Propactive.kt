package io.github.propactive.plugin

import io.github.propactive.task.GenerateApplicationProperties
import io.github.propactive.task.GenerateApplicationPropertiesTask
import org.gradle.api.Plugin
import org.gradle.api.Project

open class Propactive : Plugin<Project> {
    companion object {
        private const val DEFAULT_BUILD_TASK = "build"
    }

    override fun apply(target: Project) {
        target
            .extensions
            .create(Propactive::class.simpleName!!.lowercase(), Configuration::class.java)

        target
            .tasks
            .register(
                GenerateApplicationProperties::class.simpleName!!.replaceFirstChar(Char::lowercaseChar),
                GenerateApplicationPropertiesTask::class.java
            ) {
                target
                    .extensions
                    .findByType(Configuration::class.java)
                    ?.apply {
                        environments?.apply { it.environments = this }
                        destination?.apply { it.destination = this }
                        implementationClass?.apply { it.implementationClass = this }
                    }

                it.dependsOn(DEFAULT_BUILD_TASK)
            }
    }
}