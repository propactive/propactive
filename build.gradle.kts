import org.gradle.api.tasks.wrapper.Wrapper.DistributionType.ALL
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    val kotlinVersion = "1.6.20"

    id("jacoco")
    id("java-library")
    id("org.jetbrains.dokka") version kotlinVersion
    kotlin("jvm") version kotlinVersion apply false
}

subprojects {
    version = System.getenv("VERSION") ?: "DEV-SNAPSHOT"

    apply(plugin = "jacoco")
    apply(plugin = "java-library")
    apply(plugin = "maven-publish")
    apply(plugin = "org.jetbrains.dokka")

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
            finalizedBy(jacocoTestReport)
        }

        jacocoTestReport {
            reports {
                csv.required.set(true)
                xml.required.set(true)
            }
        }

        jar {
            withManifestDetails()
        }

        // Override sourcesJar task to include Kotlin artifacts
        val sourcesJar = register<Jar>("sourcesJar") {
            group = "build"
            withManifestDetails()
            archiveClassifier.set("sources")
            from(sourceSets["main"].allSource)
        }

        // Override javadoc task to include Dokka artifacts
        val javadocJar = register<Jar>("javadocJar") {
            group = "build"
            withManifestDetails()
            dependsOn(dokkaHtml)
            archiveClassifier.set("javadoc")
            from(dokkaHtml.get().outputDirectory)
        }

        artifacts {
            archives(sourcesJar)
            archives(javadocJar)
        }
    }

    // https://docs.gradle.org/current/userguide/java_plugin.html#sec:java-extension
    java {
        // https://blog.gradle.org/java-toolchains
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(17))
            vendor.set(JvmVendorSpec.ADOPTOPENJDK)
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

fun Jar.withManifestDetails() {
    manifest {
        attributes(
            mapOf(
                "Implementation-Title" to project.name,
                "Implementation-Version" to project.version,
                "Implementation-Vendor" to "Propactive",
            )
        )
    }
}