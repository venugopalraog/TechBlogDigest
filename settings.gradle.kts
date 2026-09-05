pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "engineering-blog-app"

include(":shared")
// androidApp is added in Phase 3; backend is its own standalone Gradle build (see backend/).
