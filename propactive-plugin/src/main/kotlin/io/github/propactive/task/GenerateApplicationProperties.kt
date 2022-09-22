package io.github.propactive.task

import io.github.propactive.environment.EnvironmentFactory
import io.github.propactive.file.FileFactory
import io.github.propactive.plugin.Configuration
import org.gradle.api.Project
import java.io.File
import java.net.URL
import java.net.URLClassLoader

object GenerateApplicationProperties {
    internal const val DEFAULT_ENVIRONMENTS = "*"
    internal const val DEFAULT_IMPLEMENTATION_CLASS = "ApplicationProperties"
    internal const val DEFAULT_BUILD_DESTINATION = "properties"

    internal val TASK_NAME =
        GenerateApplicationProperties::class.simpleName!!.replaceFirstChar(Char::lowercaseChar)
    internal val TASK_DESCRIPTION = """
        | Generates application properties file for each given environment.
        |
        | Optional configurations:
        | -P${Configuration::environments.name}
        |     Description: Comma separated list of environments to generate the properties.
        |     Example: test,stage,prod
        |     Default: $DEFAULT_ENVIRONMENTS (All provided environment)
        | -P${Configuration::implementationClass.name}
        |     Description: Sets the location of your properties object.
        |     Example: com.package.path.to.your.ApplicationProperties
        |     Default: $DEFAULT_IMPLEMENTATION_CLASS (at the root of your project)
        | -P${Configuration::destination.name}
        |     Description: Sets the location of your generated properties file within the build directory.
        |     Example: path/to/your/desired/location
        |     Default: $DEFAULT_BUILD_DESTINATION (i.e. in a directory called "properties" within your build directory)
    """.trimMargin()

    internal fun invoke(
        project: Project,
        environments: String,
        implementationClass: String,
        destination: String
    ) = project
        .getTasksByName("jar", true)
        .fold(setOf<File>()) { acc, task -> acc.plus(task.outputs.files.files) }
        .findGivenInstanceOf(implementationClass)
        ?.let(EnvironmentFactory::create)
        ?.filter { environments.contains(it.name) || environments.contains(DEFAULT_ENVIRONMENTS) }
        ?.forEach { environment -> FileFactory.create(environment, project.layout.buildDirectory.dir(destination).get().asFile.absolutePath) }
        ?: error("Expected to find implementation class $implementationClass")

    private fun Set<File>.findGivenInstanceOf(implementationClass: String) = this.firstNotNullOfOrNull {
        URLClassLoader
            .newInstance(arrayOf(URL("jar:file:${it.path}!/")), EnvironmentFactory::class.java.classLoader)
            .runCatching { loadClass(implementationClass).kotlin }
            .getOrNull()
    }
}