package io.github.propactive.support.extension.project

import java.io.File

class ProjectDirectory(
    parent: File,
    internal val buildScript: BuildScript,
    internal val mainSourceSet: MainSourceSet,
    internal val mainResourcesSet: MainResourcesSet,
    internal val buildOutput: BuildOutput,
) : File(parent.path)
