pluginManagement {
    repositories {
        gradlePluginPortal() // Add the Gradle Plugin Portal
        mavenCentral() // Good practice to include Maven Central as well
        // Add ObjectBox repositories for the plugin
        maven { url = uri("https://oss.sonatype.org/content/repositories/snapshots/") }
        maven { url = uri("https://oss.sonatype.org/content/repositories/releases/") }
    }
}

rootProject.name = "file_system"
