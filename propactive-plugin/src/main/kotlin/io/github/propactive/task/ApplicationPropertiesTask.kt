package io.github.propactive.task

import io.github.propactive.plugin.Configuration
import io.github.propactive.plugin.Configuration.Companion.DEFAULT_CONFIGURATION
import io.github.propactive.plugin.Propactive
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.FileTree
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity.RELATIVE
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSet.MAIN_SOURCE_SET_NAME
import org.gradle.api.tasks.SourceSetContainer
import java.io.File

@CacheableTask
abstract class ApplicationPropertiesTask : DefaultTask() {
    private val configuration: Configuration = project.extensions
        .findByType(Configuration::class.java)
        ?: DEFAULT_CONFIGURATION

    @get:Input
    val environments: String = configuration.environments

    @get:Input
    val implementationClass: String = configuration.implementationClass

    @get:Input
    val filenameOverride: String = configuration.filenameOverride

    /**
     * A reference used to resolve the location of the project's compiled classes.
     *
     * It's important to note this is not a live representation of the main source set's output directory.
     * But rather a reference to the location of the compiled classes used in conjunction with the
     * [io.github.propactive.task.support.PropertyClassLoader] to load the application properties
     * class, so we can effectively generate the application properties file.
     *
     * @see [compiledClassesFileTree] for how we track the compiled classes as input files for caching purposes.
     * */
    @get:Internal
    val compiledClassesDirectories: MutableSet<File> by lazy {
        project.mainSourceSetConfigs().output.files
    }

    @get:InputFiles
    @get:PathSensitive(RELATIVE)
    internal val compiledClassesFileTree: FileTree
        get() = project
            .mainSourceSetConfigs()
            .output.classesDirs.asFileTree
            .matching { it.include("**/*.class") }

    @get:OutputDirectory
    val destination: String
        get() = configuration.destination
            .takeUnless(String::isBlank)
            ?: project
                .mainSourceSetConfigs()
                .output.resourcesDir?.absolutePath
            ?: project.layout
                .buildDirectory.dir("resources/$MAIN_SOURCE_SET_NAME").get()
                .asFile.absolutePath

    /** We collect the main source set configurations instead of path guessing. */
    private fun Project.mainSourceSetConfigs(): SourceSet = extensions
        .getByName("sourceSets")
        .let { ext -> ext as SourceSetContainer }
        .getByName(MAIN_SOURCE_SET_NAME)

    init {
        group = Propactive.PROPACTIVE_GROUP
    }
}
