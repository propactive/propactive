pluginManagement {
    repositories {
        mavenLocal { url = uri("file://${System.getProperty("user.home")}/.m2/repository") }
        mavenCentral()
        gradlePluginPortal()
    }
}
