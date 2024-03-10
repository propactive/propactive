package io.github.propactive.support.extension.project

import io.github.propactive.environment.Environment
import io.github.propactive.property.Property
import io.github.propactive.support.extension.KotlinEnvironmentExtension
import io.github.propactive.type.INTEGER
import java.io.File

class MainSourceSet(parent: File, name: String = NAME) : File(parent.resolve("src"), name) {
    init { check(mkdirs() || exists()) { "Failed to create main source set directory: $this" } }

    /**
     * Write a kotlin file with the given name and content.
     */
    fun withKotlinFile(
        path: String,
        content: () -> String,
    ) = apply {
        File(this, "kotlin")
            .resolve(path)
            .apply { parentFile.mkdirs() }
            .writeText(content())
    }

    companion object {
        const val NAME = "main"
        const val APPLICATION_PROPERTIES_CLASS_NAME = "ApplicationProperties"

        /**
         * Generates a valid Propactive application properties source code.
         *
         * @param classPackagePath The package path for the class.
         * @param extraEntries Additional property entries to include in the source file.
         */
        fun applicationPropertiesKotlinSource(
            classPackagePath: String = "",
            extraEntries: List<String> = emptyList(),
        ) = """
                ${if (classPackagePath.isNotEmpty()) "package $classPackagePath" else ""}

                import ${Property::class.qualifiedName}
                import ${Environment::class.qualifiedName}
                import ${INTEGER::class.qualifiedName}

                /**
                 * This is a dummy class that is used for plugin integration tests.
                 *
                 * @see ${KotlinEnvironmentExtension::class.qualifiedName}
                 */
                @${Environment::class.simpleName}
                @Suppress("unused")
                object $APPLICATION_PROPERTIES_CLASS_NAME {
                    @${Property::class.simpleName}(["ABC"])
                    const val stringPropertyKey = "propactive.dev.string.property.key"

                    @${Property::class.simpleName}(["42"], type = ${INTEGER::class.simpleName}::class)
                    const val intPropertyKey = "propactive.dev.int.property.key"

                    ${if (extraEntries.isNotEmpty()) extraEntries.joinToString("\n") else ""}
                }
        """.trimIndent()
    }
}
