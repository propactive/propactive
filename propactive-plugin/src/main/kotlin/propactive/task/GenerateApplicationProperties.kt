package propactive.task

import org.gradle.api.Project
import propactive.environment.EnvironmentFactory
import propactive.file.FileFactory
import propactive.task.GenerateApplicationPropertiesTask.Companion.ENVIRONMENTS_WILDCARD
import java.io.File
import java.net.URL
import java.net.URLClassLoader

object GenerateApplicationProperties {
    fun invoke(
        project: Project,
        environments: String,
        implementationClass: String,
        destination: String
    ) = project
        .getTasksByName("jar", true)
        .fold(setOf<File>()) { acc, task -> acc.plus(task.outputs.files.files) }
        .findGivenInstanceOf(implementationClass)
        ?.let(EnvironmentFactory::create)
        ?.filter { environments.contains(it.name) || environments.contains(ENVIRONMENTS_WILDCARD) }
        ?.forEach { environment -> FileFactory.create(environment, destination) }
        ?: error("Expected to find implementation class $implementationClass")

    private fun Set<File>.findGivenInstanceOf(implementationClass: String) = this.firstNotNullOfOrNull {
        URLClassLoader
            .newInstance(arrayOf(URL("jar:file:${it.path}!/")), EnvironmentFactory::class.java.classLoader)
            .runCatching { loadClass(implementationClass).kotlin }
            .getOrNull()
    }
}