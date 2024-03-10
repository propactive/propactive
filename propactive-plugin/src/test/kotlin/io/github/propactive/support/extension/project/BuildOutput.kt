package io.github.propactive.support.extension.project

import java.io.File

class BuildOutput(parent: File, name: String = NAME) : File(parent, name) {
    init { check(mkdirs() || exists()) { "Failed to create build output directory: $this" } }
    companion object { const val NAME = "build" }
}
