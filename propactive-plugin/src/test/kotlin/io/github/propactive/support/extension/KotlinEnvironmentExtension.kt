package io.github.propactive.support.extension

import io.github.propactive.support.extension.EnvironmentNamespace.Component
import io.github.propactive.support.extension.gradle.TaskExecutor
import io.github.propactive.support.extension.project.BuildOutput
import io.github.propactive.support.extension.project.BuildScript
import io.github.propactive.support.extension.project.MainResourcesSet
import io.github.propactive.support.extension.project.MainSourceSet
import io.github.propactive.support.extension.project.ProjectDirectory
import io.github.propactive.support.utils.addFileToDirFromTestResources
import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.ParameterContext
import org.junit.jupiter.api.extension.ParameterResolver
import java.nio.file.Files

/**
 * A JUnit extension that provides a simulation of a Kotlin project environment.
 *
 * This extension is responsible for creating a temporary directory that simulates a Kotlin project.
 * It also provides access to the project's build script, main source set, main resources set, and
 * build output so tests can interact with them accordingly.
 *
 * It creates the following structure:
 *
 * ```
 * project-directory/
 * ├── build.gradle.kts
 * ├── settings.gradle.kts
 * ├── src/
 * │   └── main/
 * │       ├── kotlin/
 * │       │   └── ApplicationProperties.kt
 * │       └── resources/
 * │           └── log4j2.xml
 * └── build/
 *    └── [build output]
 * ```
 *
 * On top of that, it also provides a [TaskExecutor] that can be used to execute Gradle tasks
 * on the simulated project and output relevant logs to the console. This is useful for testing
 * [io.github.propactive.plugin.Propactive] tasks that interact with the project's environment.
 *
 * @see ProjectDirectory for more information about the project directory structure.
 */
class KotlinEnvironmentExtension : ParameterResolver, BeforeAllCallback, AfterAllCallback {
    private val environmentNamespace = EnvironmentNamespace()

    private val parameterTypeToRetriever = mapOf(
        TaskExecutor::class.java to this::retrieveTaskExecutor,
        ProjectDirectory::class.java to this::retrieveProjectDirectory,
        BuildScript::class.java to this::retrieveBuildScript,
        MainSourceSet::class.java to this::retrieveMainSourceSet,
        MainResourcesSet::class.java to this::retrieveMainResourcesSet,
        BuildOutput::class.java to this::retrieveBuildOutput,
    )

    override fun beforeAll(context: ExtensionContext) {
        val parent = Files.createTempDirectory("under-test-project-").toFile()

        val buildScript = with("build.gradle.kts") {
            BuildScript(parent.addFileToDirFromTestResources(this), this)
        }

        val mainSourceSet = MainSourceSet(parent.resolve("src/main/"), "kotlin").apply {
            check(mkdirs()) { "Failed to create main source set directory: $this" }
            addFileToDirFromTestResources("ApplicationProperties.kt")
        }

        val mainResourcesSet = MainResourcesSet(parent.resolve("src/main/"), "resources").apply {
            check(mkdirs()) { "Failed to create main resources set directory: $this" }
            addFileToDirFromTestResources("log4j2.xml")
        }

        val projectDirectory = ProjectDirectory(
            parent,
            buildScript,
            mainSourceSet,
            mainResourcesSet,
            BuildOutput(parent, "build"),
        ).apply {
            addFileToDirFromTestResources("settings.gradle.kts")
        }

        environmentNamespace.put(context, Component.TaskExecutor, TaskExecutor(projectDirectory))
        environmentNamespace.put(context, Component.ProjectDirectory, projectDirectory)
        environmentNamespace.put(context, Component.BuildScript, projectDirectory.buildScript)
        environmentNamespace.put(context, Component.MainSourceSet, projectDirectory.mainSourceSet)
        environmentNamespace.put(context, Component.ResourcesSourceSet, projectDirectory.mainResourcesSet)
        environmentNamespace.put(context, Component.BuildOutput, projectDirectory.buildOutput)
    }

    override fun afterAll(context: ExtensionContext) {
        environmentNamespace.apply {
            remove<TaskExecutor>(context, Component.TaskExecutor)
            remove<ProjectDirectory>(context, Component.ProjectDirectory)?.deleteRecursively()
            remove<BuildScript>(context, Component.BuildScript)
            remove<MainSourceSet>(context, Component.MainSourceSet)
            remove<MainResourcesSet>(context, Component.ResourcesSourceSet)
            remove<BuildOutput>(context, Component.BuildOutput)
        }
    }

    override fun supportsParameter(parameterContext: ParameterContext, extensionContext: ExtensionContext) =
        parameterTypeToRetriever
            .map { it.key }
            .any(parameterContext.parameter.type::isAssignableFrom)

    override fun resolveParameter(parameterContext: ParameterContext, extensionContext: ExtensionContext) =
        checkNotNull(parameterTypeToRetriever[parameterContext.parameter.type]) {
            "Unsupported parameter type: ${parameterContext.parameter.type}"
        }(extensionContext)

    private fun retrieveTaskExecutor(context: ExtensionContext) = environmentNamespace
        .get<TaskExecutor>(context, Component.TaskExecutor)

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
}
