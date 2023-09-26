@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.gradle.publish)
}

val isDevVersion = "$version" == "DEV-SNAPSHOT"
val isSemVersioned = "$version".matches(Regex("[0-9]+\\.[0-9]+\\.[0-9]+.*?"))
val isVersionedSnapshot = isSemVersioned && "$version".endsWith("-SNAPSHOT")
val isVersionedRelease = isSemVersioned && isVersionedSnapshot.not()

gradlePlugin {
    website.set("https://github.com/propactive/propactive/blob/main/README.md")
    vcsUrl.set("https://github.com/propactive/propactive.git")

    plugins {
        create("propactive") {
            id = project.group.toString()
            displayName = project.name
            description = project.description.toString()
            implementationClass = "${project.group}.plugin.Propactive"
            tags.set(listOf("properties", "property-management", "property-testing", "application.properties"))
        }
    }
}

dependencies {
    api(project(":propactive-jvm"))

    implementation(gradleApi())
    testImplementation(gradleTestKit())
}

signing {
    // Sign only if it's a versioned-release build as -SNAPSHOT plugin versions
    // not supported for Gradle Plugin Portal...
    setRequired { isVersionedRelease }
    if (isRequired) useGpgCmd()
    sign(configurations.archives.get())
}

// The Gradle Nexus plugin has a bug that requires us to manually
// configure the signing tasks to run before the publishing tasks.
// see: https://github.com/gradle-nexus/publish-plugin/issues/208
val signingTasks: TaskCollection<Sign> = tasks.withType<Sign>()
tasks.withType<PublishToMavenRepository>().configureEach { mustRunAfter(signingTasks) }

tasks {
    publishPlugins { onlyIf { isVersionedRelease } }
}
