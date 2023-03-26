include(
    ":propactive-plugin",
    ":propactive-jvm",
)

dependencyResolutionManagement {
    enableFeaturePreview("VERSION_CATALOGS")
    versionCatalogs {
        create("libs") {
            val kotlinVersion = "1.8.0"
            val dokkaVersion = "1.8.10"
            val serializationVersion = "1.4.1"
            val mockkVersion = "1.13.2"
            val kotestVersion = "5.5.4"
            val junitVersion = "5.8.2"
            val ktlintVersion = "11.0.0"
            val publishVersion = "1.0.0"
            val publishNexusVersion = "1.1.0"
            val equalsVersion = "3.10"
            val log4jKotlinVersion = "1.2.0" // TODO: Upgrade to 1.3.0 if my PR is merged
            val log4jVersion = "2.19.0"

            library("kotlin-reflect", "org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")
            library("kotlin-stdlib", "org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
            library("kotlinx-serialization-json", "org.jetbrains.kotlinx:kotlinx-serialization-json:$serializationVersion")
            library("mockk", "io.mockk:mockk:$mockkVersion")
            library("kotest-runner-junit5", "io.kotest:kotest-runner-junit5:$kotestVersion")
            library("kotest-assertions-core", "io.kotest:kotest-assertions-core:$kotestVersion")
            library("junit-jupiter", "org.junit.jupiter:junit-jupiter:$junitVersion")
            library("equalsverifier", "nl.jqno.equalsverifier:equalsverifier:$equalsVersion")

            library("log4j-api-kotlin", "org.apache.logging.log4j:log4j-api-kotlin:$log4jKotlinVersion")
            library("log4j-core", "org.apache.logging.log4j:log4j-core:$log4jVersion")

            plugin("jetbrains-kotlin-jvm", "org.jetbrains.kotlin.jvm").version(kotlinVersion)
            plugin("jetbrains-dokka", "org.jetbrains.dokka").version(dokkaVersion)
            plugin("gradle-ktlint", "org.jlleitschuh.gradle.ktlint").version(ktlintVersion)
            plugin("gradle-publish", "com.gradle.plugin-publish").version(publishVersion)
            plugin("gradle-publish-nexus", "io.github.gradle-nexus.publish-plugin").version(publishNexusVersion)
        }
    }
}
