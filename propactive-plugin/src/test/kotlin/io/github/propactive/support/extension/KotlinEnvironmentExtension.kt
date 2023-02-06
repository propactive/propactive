package io.github.propactive.support.extension

import io.github.propactive.support.extension.EnvironmentNamespace.Component
import io.github.propactive.support.extension.project.BuildOutput
import io.github.propactive.support.extension.project.BuildScript
import io.github.propactive.support.extension.project.MainResourcesSet
import io.github.propactive.support.extension.project.MainSourceSet
import io.github.propactive.support.extension.project.ProjectDirectory
import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.ParameterContext
import org.junit.jupiter.api.extension.ParameterResolver
import java.io.File
import java.nio.file.Files

class KotlinEnvironmentExtension : ParameterResolver, BeforeAllCallback, AfterAllCallback {
    private val environmentNamespace = EnvironmentNamespace()

    private val parameterTypeToRetriever = mapOf(
        ProjectDirectory::class.java to this::retrieveProjectDirectory,
        BuildScript::class.java to this::retrieveBuildScript,
        MainSourceSet::class.java to this::retrieveMainSourceSet,
        MainResourcesSet::class.java to this::retrieveMainResourcesSet,
        BuildOutput::class.java to this::retrieveBuildOutput,
    )

    override fun supportsParameter(parameterContext: ParameterContext, extensionContext: ExtensionContext) =
        parameterTypeToRetriever
            .map { it.key }
            .any { parameterContext.parameter.type.isAssignableFrom(it) }

    override fun resolveParameter(parameterContext: ParameterContext, extensionContext: ExtensionContext) =
        (
            parameterTypeToRetriever[parameterContext.parameter.type]
                ?: error("Unsupported parameter context '$parameterContext'")
            )(extensionContext)

    override fun beforeAll(context: ExtensionContext) {
        val projectDirectory = ProjectDirectory(
            Files.createTempDirectory("under-test-project-").toFile()
        ).apply {
            withResource("settings.gradle.kts")
            withResource("build.gradle.kts")
            withResource("ApplicationProperties.kt", mainSourceSet.path)
            withResource("log4j2.xml", mainResourcesSet.path)
        }

        environmentNamespace.put(context, Component.ProjectDirectory, projectDirectory)
        environmentNamespace.put(context, Component.BuildScript, projectDirectory.buildScript)
        environmentNamespace.put(context, Component.MainSourceSet, projectDirectory.mainSourceSet)
        environmentNamespace.put(context, Component.ResourcesSourceSet, projectDirectory.mainResourcesSet)
        environmentNamespace.put(context, Component.BuildOutput, projectDirectory.buildOutput)
    }

    override fun afterAll(context: ExtensionContext) {
        environmentNamespace.apply {
            remove<ProjectDirectory>(context, Component.ProjectDirectory)?.deleteRecursively()
            remove<BuildScript>(context, Component.BuildScript)
            remove<MainSourceSet>(context, Component.MainSourceSet)
            remove<MainResourcesSet>(context, Component.ResourcesSourceSet)
            remove<BuildOutput>(context, Component.BuildOutput)
        }
    }

    private fun retrieveProjectDirectory(context: ExtensionContext) = environmentNamespace
        .get<ProjectDirectory>(context, Component.ProjectDirectory)

    private fun retrieveBuildScript(context: ExtensionContext) = environmentNamespace
        .get<BuildScript>(context, Component.BuildScript)

    private fun retrieveMainSourceSet(context: ExtensionContext) = environmentNamespace
        .get<MainSourceSet>(context, Component.MainSourceSet)

    private fun retrieveMainResourcesSet(context: ExtensionContext) = environmentNamespace
        .get<MainResourcesSet>(context, Component.ResourcesSourceSet)

    private fun retrieveBuildOutput(context: ExtensionContext) = environmentNamespace
        .get<BuildOutput>(context, Component.BuildOutput)

    private fun File.withResource(
        name: String,
        path: String? = null,
    ) = this.apply {
        KotlinEnvironmentExtension::class.java
            .classLoader
            .getResource(name)
            .let { requireNotNull(it) { "Resource not found: $name" } }
            .readBytes()
            .also { content ->
                File(path ?: this.path, name)
                    .apply { parentFile.mkdirs() }
                    .writeBytes(content)
            }
    }
}
