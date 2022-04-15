import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    val kotlinVersion = "1.6.20"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion
}

group = "propactive"
version = System.getenv("VERSION") ?: "DEV-SNAPSHOT"

repositories(RepositoryHandler::mavenCentral)

dependencies {
    val kotlinVersion: String by project
    val kotlinxSerializationJsonVersion: String by project
    val mockkVersion: String by project
    val kotestVersion: String by project
    val equalsVerifierVersion: String by project
    val jupiterVersion: String by project

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinxSerializationJsonVersion")
    implementation("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")

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

    withType<KotlinCompile>().configureEach { kotlinOptions.jvmTarget = "17" }
}
