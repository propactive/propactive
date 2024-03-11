package io.github.propactive.support.extension.project

import io.github.propactive.plugin.Configuration.Companion.DEFAULT_AUTO_GENERATE_APPLICATION_PROPERTIES
import io.github.propactive.plugin.Configuration.Companion.DEFAULT_BUILD_DESTINATION
import io.github.propactive.plugin.Configuration.Companion.DEFAULT_CLASS_COMPILE_DEPENDENCY
import io.github.propactive.plugin.Configuration.Companion.DEFAULT_ENVIRONMENTS
import io.github.propactive.plugin.Configuration.Companion.DEFAULT_FILENAME_OVERRIDE
import io.github.propactive.plugin.Configuration.Companion.DEFAULT_IMPLEMENTATION_CLASS
import java.io.File

class BuildScript(parent: File, name: String) : File(parent, name) {
    /**
     * Creates a [BUILD_SCRIPT_KTS] file with the given Propactive configurations.
     */
    fun asKts(
        environments: String = DEFAULT_ENVIRONMENTS,
        implementationClass: String = DEFAULT_IMPLEMENTATION_CLASS,
        destination: String = DEFAULT_BUILD_DESTINATION,
        filenameOverride: String = DEFAULT_FILENAME_OVERRIDE,
        classCompileDependency: String = DEFAULT_CLASS_COMPILE_DEPENDENCY,
        autoGenerateApplicationProperties: Boolean = DEFAULT_AUTO_GENERATE_APPLICATION_PROPERTIES,
    ) = apply {
        writeText(
            """
            import org.gradle.api.tasks.wrapper.Wrapper.DistributionType.ALL
            import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

            plugins {
                java
                id("io.github.propactive") version "DEV-SNAPSHOT"
                kotlin("jvm") version "1.9.22"
            }

            propactive {
                environments = "$environments"
                implementationClass = "$implementationClass"
                destination = "$destination"
                filenameOverride = "$filenameOverride"
                classCompileDependency = "$classCompileDependency"
                autoGenerateApplicationProperties = $autoGenerateApplicationProperties
            }

            repositories {
                mavenCentral()
                mavenLocal()
            }

            dependencies {
                implementation("io.github.propactive:propactive-jvm:DEV-SNAPSHOT")
            }

            tasks {
                wrapper {
                    distributionType = ALL
                }

                withType<KotlinCompile>().configureEach {
                    kotlinOptions.jvmTarget = "21"
                }
            }

            java {
                toolchain {
                    languageVersion.set(JavaLanguageVersion.of(21))
                }
            }
            """.trimIndent(),
        )
    }

    companion object {
        const val BUILD_SCRIPT_KTS = "build.gradle.kts"
    }
}
