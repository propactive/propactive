package io.github.propactive.support.extension

import io.github.propactive.support.extension.EnvironmentNamespace.Component
import io.github.propactive.support.extension.gradle.TaskExecutor
import io.github.propactive.support.extension.project.BuildOutput
import io.github.propactive.support.extension.project.BuildScript
import io.github.propactive.support.extension.project.BuildScript.Companion.BUILD_SCRIPT_KTS
import io.github.propactive.support.extension.project.MainResourcesSet
import io.github.propactive.support.extension.project.MainSourceSet
import io.github.propactive.support.extension.project.MainSourceSet.Companion.APPLICATION_PROPERTIES_CLASS_NAME
import io.github.propactive.support.extension.project.MainSourceSet.Companion.applicationPropertiesKotlinSource
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
        ProjectDirectory::class.java to this::retrieveProjectDirectory,
        BuildScript::class.java to this::retrieveBuildScript,
        MainSourceSet::class.java to this::retrieveMainSourceSet,
        MainResourcesSet::class.java to this::retrieveMainResourcesSet,
        BuildOutput::class.java to this::retrieveBuildOutput,
        TaskExecutor::class.java to this::retrieveTaskExecutor,
    )

    override fun beforeAll(context: ExtensionContext) {
        val root = Files
            .createTempDirectory("under-test-project-")
            .toFile()
        val projectDirectory = ProjectDirectory(
            root.addFileToDirFromTestResources("settings.gradle.kts"),
            BuildScript(root, BUILD_SCRIPT_KTS).asKts(),
            MainSourceSet(root).withKotlinFile(APPLICATION_PROPERTIES_CLASS_NAME.plus(".kt"), ::applicationPropertiesKotlinSource),
            MainResourcesSet(root).addFileToDirFromTestResources("log4j2.xml"),
            BuildOutput(root),
        )

        environmentNamespace.put(context, Component.ProjectDirectory, projectDirectory)
        environmentNamespace.put(context, Component.BuildScript, projectDirectory.buildScript)
        environmentNamespace.put(context, Component.MainSourceSet, projectDirectory.mainSourceSet)
        environmentNamespace.put(context, Component.ResourcesSourceSet, projectDirectory.mainResourcesSet)
        environmentNamespace.put(context, Component.BuildOutput, projectDirectory.buildOutput)
    }

    override fun afterAll(context: ExtensionContext) {
        environmentNamespace.apply {
            remove<BuildScript>(context, Component.BuildScript)
            remove<MainSourceSet>(context, Component.MainSourceSet)
            remove<MainResourcesSet>(context, Component.ResourcesSourceSet)
            remove<BuildOutput>(context, Component.BuildOutput)
            remove<ProjectDirectory>(context, Component.ProjectDirectory)?.deleteRecursively()
        }
    }

    override fun supportsParameter(parameterContext: ParameterContext, extensionContext: ExtensionContext) =
        parameterTypeToRetriever
            .map { entry -> entry.key }
            .any(parameterContext.parameter.type::isAssignableFrom)

    override fun resolveParameter(parameterContext: ParameterContext, extensionContext: ExtensionContext) =
        checkNotNull(parameterTypeToRetriever[parameterContext.parameter.type]) {
            "Unsupported parameter type: ${parameterContext.parameter.type}"
        }(extensionContext)

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

    private fun retrieveTaskExecutor(context: ExtensionContext) = TaskExecutor(
        retrieveProjectDirectory(context)!!,
    )
}
