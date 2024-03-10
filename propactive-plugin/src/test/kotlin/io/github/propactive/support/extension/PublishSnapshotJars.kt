package io.github.propactive.support.extension

import org.gradle.tooling.GradleConnector
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.ExtensionContext
import java.io.File

/**
 * Publishes Propactive snapshot jars to the local Maven repository before the tests are executed.
 * This is most useful when running tests in a CI environment where the jars are not published to the local Maven repository.
 */
class PublishSnapshotJars : BeforeAllCallback {
    override fun beforeAll(context: ExtensionContext) {
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
