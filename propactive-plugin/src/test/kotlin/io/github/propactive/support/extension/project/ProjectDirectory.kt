package io.github.propactive.support.extension.project

import java.io.File

class ProjectDirectory(
    parent: File,
) : File(parent.path) {
    internal val buildScript = BuildScript(this, "build.gradle.kts")
    internal val mainSourceSet = MainSourceSet(this.resolve("src/main/"), "kotlin")
    internal val mainResourcesSet = MainResourcesSet(this.resolve("src/main/"), "resources")
    internal val buildOutput = BuildOutput(this, "build")
}
