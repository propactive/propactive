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

tasks {
    publishPlugins { onlyIf { isVersionedRelease } }
}
