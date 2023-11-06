include(
    ":propactive-plugin",
    ":propactive-jvm",
)

/**
 * The org.gradle.disco-toolchains plugin provides a repository to help auto-downloading
 * JVMs. It is based on the foojay DiscoAPI. Requires Gradle 7.6 or later to work.
 *
 *  See:
 *  - https://docs.gradle.org/current/userguide/toolchains.html#sub:download_repositories
 *  - https://github.com/gradle/foojay-toolchains
 */
plugins {
    val fooJayPluginVersion = "0.7.0"
    id("org.gradle.toolchains.foojay-resolver-convention") version fooJayPluginVersion
}

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            val kotlinVersion = "1.9.20"
            val dokkaVersion = "1.9.10" // TODO Upgrade to 1.9.20 when it is released (see https://github.com/Kotlin/dokka/milestone/30 )
            val serializationVersion = "1.6.0"
            val mockkVersion = "1.13.8"
            val kotestVersion = "5.7.2"
            val junitVersion = "5.10.0"
            val ktlintVersion = "11.6.1"
            val publishVersion = "1.2.1"
            val publishNexusVersion = "1.3.0"
            val equalsVersion = "3.15.3"
            val log4jKotlinVersion = "1.3.0"
            val log4jVersion = "2.21.1"

            library("kotlin-reflect", "org.jetbrains.kotlin", "kotlin-reflect").version(kotlinVersion)
            library("kotlin-stdlib", "org.jetbrains.kotlin", "kotlin-stdlib").version(kotlinVersion)
            library("kotlinx-serialization-json", "org.jetbrains.kotlinx", "kotlinx-serialization-json").version(serializationVersion)
            library("mockk", "io.mockk", "mockk").version(mockkVersion)
            library("kotest-runner-junit5", "io.kotest", "kotest-runner-junit5").version(kotestVersion)
            library("kotest-assertions-core", "io.kotest", "kotest-assertions-core").version(kotestVersion)
            library("junit-jupiter", "org.junit.jupiter", "junit-jupiter").version(junitVersion)
            library("equalsverifier", "nl.jqno.equalsverifier", "equalsverifier").version(equalsVersion)
            library("log4j-api-kotlin", "org.apache.logging.log4j", "log4j-api-kotlin").version(log4jKotlinVersion)
            library("log4j-core", "org.apache.logging.log4j", "log4j-core").version(log4jVersion)

            plugin("jetbrains-kotlin-jvm", "org.jetbrains.kotlin.jvm").version(kotlinVersion)
            plugin("jetbrains-dokka", "org.jetbrains.dokka").version(dokkaVersion)
            plugin("gradle-ktlint", "org.jlleitschuh.gradle.ktlint").version(ktlintVersion)
            plugin("gradle-publish", "com.gradle.plugin-publish").version(publishVersion)
            plugin("gradle-publish-nexus", "io.github.gradle-nexus.publish-plugin").version(publishNexusVersion)
        }
    }
}
