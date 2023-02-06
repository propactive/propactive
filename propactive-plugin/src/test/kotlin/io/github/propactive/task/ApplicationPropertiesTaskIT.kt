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
    internal val taskUnderTest: String
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
        //   These tests depends on the task "publishToMavenLocal" being run before the tests are executed.
        //   Which will publish the jars locally, so we can use them in tests through MavenLocal.
        GradleConnector
            .newConnector()
            .forProjectDirectory(propactiveRootProject)
            .connect()
            .use { gradle ->
                gradle
                    .newBuild()
                    .forTasks("publishToMavenLocal")
                    .setStandardOutput(System.out)
                    .setStandardError(System.err)
                    .run()
            }
    }
}
