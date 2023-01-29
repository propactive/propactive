@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    `java-gradle-plugin`
    alias(libs.plugins.gradle.publish)
}

val isDevVersion = "$version" == "DEV-SNAPSHOT"
val isSemVersioned = "$version".matches(Regex("[0-9]+\\.[0-9]+\\.[0-9]+.*?"))
val isVersionedSnapshot = isSemVersioned && "$version".endsWith("-SNAPSHOT")
val isVersionedRelease = isSemVersioned && isVersionedSnapshot.not()

gradlePlugin {
    plugins {
        create("propactive") {
            id = project.group.toString()
            displayName = project.name
            description = project.description.toString()
            implementationClass = "${project.group}.plugin.Propactive"
        }
    }
}

pluginBundle {
    website = "https://github.com/u-ways/propactive/blob/main/README.md"
    vcsUrl = "https://github.com/u-ways/propactive.git"
    tags = listOf("properties", "property-management", "property-testing", "application.properties")
}

dependencies {
    api(project(":propactive-jvm"))
    api(project(":propactive-logging"))

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
