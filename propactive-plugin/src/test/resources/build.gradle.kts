import org.gradle.api.tasks.wrapper.Wrapper.DistributionType.ALL
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    val kotlinVersion = "1.8.10"
    val propactiveVersion = "DEV-SNAPSHOT"

    id("java-library")
    id("io.github.propactive") version propactiveVersion
    kotlin("jvm") version kotlinVersion
}

propactive {
    implementationClass = "ApplicationProperties"
    filenameOverride = "application.properties"
    destination = layout.buildDirectory.dir("properties").get().asFile.absolutePath
}

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    val propactiveVersion = "DEV-SNAPSHOT"
    val log4jKotlinVersion = "1.2.0" // TODO: Upgrade to 1.3.0 if my PR is merged
    val log4jVersion = "2.19.0"

    implementation("io.github.propactive:propactive-jvm:$propactiveVersion")
    implementation("org.apache.logging.log4j:log4j-api-kotlin:$log4jKotlinVersion")
    implementation("org.apache.logging.log4j:log4j-core:$log4jVersion")
}

tasks {
    wrapper {
        distributionType = ALL
    }

    withType<KotlinCompile>().configureEach {
        kotlinOptions.jvmTarget = "17"
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}
