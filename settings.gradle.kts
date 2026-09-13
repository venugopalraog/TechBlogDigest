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
include(":androidApp")
include(":webApp")
// backend is its own standalone Gradle build (see backend/).
