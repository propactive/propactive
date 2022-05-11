import org.gradle.api.tasks.wrapper.Wrapper.DistributionType.ALL
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    val kotlinVersion = "1.6.20"

    id("java-library")
    kotlin("jvm") version kotlinVersion apply false
}

subprojects {
    version = System.getenv("VERSION") ?: "DEV-SNAPSHOT"

    apply(plugin = "java-library")
    apply(plugin = "maven-publish")

    repositories(RepositoryHandler::mavenCentral)

    dependencies {
        val mockkVersion: String by project
        val kotestVersion: String by project
        val equalsVerifierVersion: String by project
        val jupiterVersion: String by project

        testImplementation("io.mockk:mockk:$mockkVersion")
        testImplementation("io.kotest:kotest-runner-junit5:$kotestVersion")
        testImplementation("io.kotest:kotest-assertions-core:$kotestVersion")
        testImplementation("nl.jqno.equalsverifier:equalsverifier:$equalsVerifierVersion")
        testImplementation("org.junit.jupiter:junit-jupiter:$jupiterVersion")
    }

    tasks {
        test {
            useJUnitPlatform()
            testLogging { showStandardStreams = true }
        }
    }
}

tasks {
    wrapper {
        distributionType = ALL
    }

    withType<KotlinCompile>().configureEach {
        kotlinOptions.jvmTarget = "17"
    }
}