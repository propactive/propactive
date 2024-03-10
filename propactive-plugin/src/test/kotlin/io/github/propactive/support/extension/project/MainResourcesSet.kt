package io.github.propactive.support.extension.project

import java.io.File

class MainResourcesSet(parent: File, name: String = NAME) : File(parent.resolve("src/main"), name) {
    init { check(mkdirs() || exists()) { "Failed to create main resources set directory: $this" } }
    companion object { const val NAME = "resources" }
}
