apply(plugin = "maven-publish")
apply(plugin = "org.jetbrains.kotlin.jvm")

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

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            groupId = project.group.toString()
            artifactId = project.name
            version = project.version.toString()
            description = project.description.toString()

            from(components["java"])
        }
    }
}