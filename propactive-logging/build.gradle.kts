plugins {
    `maven-publish`
}

val isDevVersion = "$version" == "DEV-SNAPSHOT"
val isSemVersioned = "$version".matches(Regex("[0-9]+\\.[0-9]+\\.[0-9]+.*?"))
val isVersionedSnapshot = isSemVersioned && "$version".endsWith("-SNAPSHOT")
val isVersionedRelease = isSemVersioned && isVersionedSnapshot.not()

dependencies {
    implementation(libs.kotlin.reflect)

    // NOTE:
    //   - If you want to access log4j2 dependencies,
    //     then declare this submodule as an API configuration.
    //   - i.e. api(project(":propactive-logging"))
    api(rootProject.libs.log4j.api.kotlin)
    api(rootProject.libs.log4j.core)
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

            repositories {
                maven {
                    val releases = "https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/"
                    val snapshot = "https://s01.oss.sonatype.org/content/repositories/snapshots/"

                    name = "sonatypeStaging"
                    url = when {
                        isVersionedRelease -> uri(releases)
                        isVersionedSnapshot -> uri(snapshot)
                        else -> mavenLocal().url
                    }

                    credentials {
                        username = System.getenv("OSSRH_USERNAME")
                        password = System.getenv("OSSRH_PASSWORD")
                    }
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

tasks {
    publish {
        onlyIf { isVersionedRelease || isVersionedSnapshot }
    }
}
