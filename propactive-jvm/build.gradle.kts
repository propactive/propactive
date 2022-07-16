plugins {
    id("signing")
}

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
        val projectUrl = "https://github.com/propactive/propactive"

        create<MavenPublication>("mavenJava") {
            groupId = project.group.toString()
            artifactId = project.name
            version = project.version.toString()

            from(components["java"])

            pom {
                name.set(project.name)
                description.set(project.description)
                url.set(projectUrl)
                scm {
                    url.set(projectUrl)
                    connection.set("scm:git:$projectUrl")
                    developerConnection.set("scm:git:$projectUrl")
                }
                issueManagement {
                    system.set("GitHub")
                    url.set("$projectUrl/issues")
                }
                licenses {
                    license {
                        name.set("MIT")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }
                developers {
                    developer {
                        id.set("u-ways")
                        name.set("U-ways E.")
                        email.set("ue95.career@gmail.com")
                        url.set("https://github.com/u-ways")
                    }
                }
            }

            repositories {
                maven {
                    name = "sonatypeStaging"
                    url = uri("https://oss.sonatype.org/service/local/staging/deploy/maven2")
                    credentials {
                        username = System.getenv("SONATYPE_USERNAME")
                        password = System.getenv("SONATYPE_PASSWORD")
                    }
                }
            }
        }
    }
}

signing {
    useGpgCmd()
    sign(configurations.archives.get())
}