import org.gradle.api.JavaVersion.VERSION_17
import org.gradle.api.tasks.wrapper.Wrapper.DistributionType.ALL
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    `java-library`
    jacoco
    signing
    alias(libs.plugins.gradle.ktlint)
    alias(libs.plugins.jetbrains.dokka)
    alias(libs.plugins.jetbrains.kotlin.jvm) apply false
}

allprojects {
    group = "io.github.propactive"
    version = System.getenv("VERSION") ?: "DEV-SNAPSHOT"
    description = "An application property generator framework that validates your property values on runtime."

    repositories(RepositoryHandler::mavenCentral)
}

subprojects {
    apply<JavaLibraryPlugin>()
    apply<JacocoPlugin>()
    apply<SigningPlugin>()
    apply<MavenPublishPlugin>()
    apply(plugin = rootProject.libs.plugins.jetbrains.kotlin.jvm.get().pluginId)
    apply(plugin = rootProject.libs.plugins.jetbrains.dokka.get().pluginId)
    apply(plugin = rootProject.libs.plugins.gradle.ktlint.get().pluginId)

    dependencies {
        implementation(rootProject.libs.log4j.api.kotlin)
        testImplementation(rootProject.libs.log4j.core)
        testImplementation(rootProject.libs.mockk)
        testImplementation(rootProject.libs.junit.jupiter)
        testImplementation(rootProject.libs.kotest.assertions.core)
        testImplementation(rootProject.libs.kotest.runner.junit5)
        testImplementation(rootProject.libs.equalsverifier)
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

    ktlint {
        verbose.set(true)
    }

    // https://docs.gradle.org/current/userguide/java_plugin.html#sec:java-extension
    java {
        // https://blog.gradle.org/java-toolchains
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(17))
        }
    }
}

tasks {
    wrapper {
        distributionType = ALL
    }

    withType<KotlinCompile>().configureEach {
        kotlinOptions.jvmTarget = VERSION_17.toString()
    }
}

fun Jar.withManifestDetails() {
    manifest.apply {
        attributes["Implementation-Title"] = project.name
        attributes["Implementation-Version"] = project.version
        attributes["Implementation-Vendor"] = "Propactive"
    }
}
