package io.github.propactive.task

import io.github.propactive.support.extension.KotlinEnvironmentExtension
import org.gradle.tooling.GradleConnector
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import org.junit.jupiter.api.TestMethodOrder
import org.junit.jupiter.api.extension.ExtendWith
import java.io.File

@TestInstance(PER_CLASS)
@TestMethodOrder(OrderAnnotation::class)
@ExtendWith(KotlinEnvironmentExtension::class)
abstract class ApplicationPropertiesTaskIT(
    internal val taskUnderTest: String,
) {

    @BeforeAll
    fun publishSnapshotJars() {
        // NOTE:
        //   We are relying on the fact that this sub-module is 1 level down the root project.
        val propactiveRootProject = System
            .getProperty("user.dir")
            .let(::File)
            .parentFile

        // NOTE:
        //   Sometimes a docker image might be started from Root but executing user might be different.
        //   To ensure that the tests are executed with the same user as the docker image, we are using the
        //   HOME environment variable to allow defining a custom user home directory for Docker images.
        val designatedHomeDir = System
            .getenv("HOME")

        // NOTE:
        //   These tests depends on the task "publishToMavenLocal" being run before the tests are executed.
        //   Which will publish the jars locally, so we can use them in tests through MavenLocal.
        GradleConnector
            .newConnector()
            .useGradleUserHomeDir(designatedHomeDir.let(::File))
            .forProjectDirectory(propactiveRootProject)
            .connect()
            .use { gradle ->
                gradle
                    .newBuild()
                    .setJvmArguments("-Duser.home=$designatedHomeDir")
                    .setEnvironmentVariables(mapOf("HOME" to designatedHomeDir, "GRADLE_USER_HOME" to designatedHomeDir.plus("/.gradle")))
                    .forTasks("publishToMavenLocal")
                    .setStandardOutput(System.out)
                    .setStandardError(System.err)
                    .run()
            }
    }
}
