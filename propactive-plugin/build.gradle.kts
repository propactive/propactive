plugins {
    val pluginPublishVersion = "0.21.0"

    id("java-gradle-plugin")
    id("com.gradle.plugin-publish") version pluginPublishVersion
}

apply(plugin = "org.jetbrains.kotlin.jvm")

gradlePlugin {
    plugins {
        create("propactive") {
            id = project.group.toString()
            displayName = project.name
            description = project.description.toString()
            implementationClass = "propactive.plugin.Propactive"
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

    implementation(gradleApi())
    testImplementation(gradleTestKit())
}