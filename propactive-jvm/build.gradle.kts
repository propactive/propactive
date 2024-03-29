plugins {
    `maven-publish`
}

val isDevVersion = "$version" == "DEV-SNAPSHOT"
val isSemVersioned = "$version".matches(Regex("[0-9]+\\.[0-9]+\\.[0-9]+.*?"))
val isVersionedSnapshot = isSemVersioned && "$version".endsWith("-SNAPSHOT")
val isVersionedRelease = isSemVersioned && isVersionedSnapshot.not()

dependencies {
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlin.reflect)
    implementation(libs.kotlin.stdlib)
    implementation(libs.log4j.core)
    implementation(libs.log4j.api.kotlin)
}

/**
 * NOTE:
 *  In order to ensure a minimum level of quality of the components available
 *  in the Central Repository, a number of deployment requirements must be
 *  met, see: https://central.sonatype.org/publish/requirements/
 */
publishing {
    publications {
        val projectUrl = "https://github.com/propactive/propactive"

        create<MavenPublication>("sonatype") {
            groupId = project.group.toString()
            artifactId = project.name
            version = project.version.toString()

            // https://docs.gradle.org/current/userguide/java_plugin.html#sec:java_plugin_publishing
            from(components["java"])
            artifact(tasks["javadocJar"])
            artifact(tasks["sourcesJar"])

            pom {
                name.set(project.name)
                description.set(project.description)
                inceptionYear.set("2022")
                url.set(projectUrl)
                scm {
                    url.set(projectUrl)
                    connection.set("scm:git:$projectUrl")
                    developerConnection.set("scm:git:$projectUrl")
                }
                issueManagement {
                    system.set("GitHub")
                    url.set("$projectUrl/issues")
                }
                licenses {
                    license {
                        name.set("MIT")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }
                developers {
                    developer {
                        id.set("u-ways")
                        name.set("U-ways E.")
                        email.set("ue95.career@gmail.com")
                        url.set("https://github.com/u-ways")
                    }
                }
                distributionManagement {
                    downloadUrl.set("$projectUrl/releases")
                }
            }
        }
    }
}

signing {
    // Signing only if it's a sem-versioned build
    // See: https://docs.gradle.org/current/userguide/signing_plugin.html#sec:conditional_signing
    setRequired { isVersionedRelease || isVersionedSnapshot }
    if (isRequired) useGpgCmd()
    // https://docs.gradle.org/current/userguide/signing_plugin.html#sec:publishing_the_signatures
    sign(publishing.publications["sonatype"])
}

// The Gradle Nexus plugin has a bug that requires us to manually
// configure the signing tasks to run before the publishing tasks.
// see: https://github.com/gradle-nexus/publish-plugin/issues/208
val signingTasks: TaskCollection<Sign> = tasks.withType<Sign>()
tasks.withType<PublishToMavenRepository>().configureEach { mustRunAfter(signingTasks) }

tasks {
    publish {
        onlyIf { isVersionedRelease || isVersionedSnapshot }
    }
}
