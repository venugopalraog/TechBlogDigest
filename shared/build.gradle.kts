plugins {
    kotlin("multiplatform") version "2.0.21"
    kotlin("plugin.serialization") version "2.0.21"
    id("com.android.library") version "8.5.2"
    id("app.cash.sqldelight") version "2.0.2"
}

kotlin {
    jvm()

    androidTarget {
        compilations.all {
            compileTaskProvider.configure {
                compilerOptions.jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
            }
        }
    }

    // iOS targets (iosX64, iosArm64, iosSimulatorArm64) are added when this module is
    // next built from macOS with Xcode installed — Kotlin/Native can't cross-compile
    // Apple targets from Windows. iosMain/ is kept as a placeholder for that actual
    // implementation (DriverFactory, HTTP engine) once a Mac is available.

    sourceSets {
        val commonMain by getting {
            kotlin.srcDir("commonMain")
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.1")
                implementation("io.ktor:ktor-client-core:3.0.1")
                implementation("io.ktor:ktor-client-content-negotiation:3.0.1")
                implementation("io.ktor:ktor-serialization-kotlinx-json:3.0.1")
                implementation("app.cash.sqldelight:runtime:2.0.2")
                implementation("app.cash.sqldelight:coroutines-extensions:2.0.2")
            }
        }
        val commonTest by getting {
            kotlin.srcDir("commonTest")
            dependencies {
                implementation(kotlin("test"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
            }
        }
        val androidMain by getting {
            kotlin.srcDir("androidMain")
            dependencies {
                implementation("io.ktor:ktor-client-okhttp:3.0.1")
                implementation("app.cash.sqldelight:android-driver:2.0.2")
            }
        }
        val jvmMain by getting {
            kotlin.srcDir("jvmMain")
            dependencies {
                implementation("io.ktor:ktor-client-cio:3.0.1")
                implementation("app.cash.sqldelight:sqlite-driver:2.0.2")
            }
        }
        val jvmTest by getting {
            kotlin.srcDir("jvmTest")
        }
    }
}

android {
    namespace = "com.engblog.shared"
    compileSdk = 34
    defaultConfig {
        minSdk = 24
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

sqldelight {
    databases {
        create("AppDatabase") {
            packageName.set("com.engblog.shared.db")
            srcDirs.setFrom("commonMain/sqldelight")
        }
    }
}

// No .sqm migration files exist yet (schema is v1), but this task still tries to open a
// scratch SQLite file via sqlite-jdbc's native lib to verify against — which fails to load
// on this JDK/host. Nothing to verify yet, so skip it rather than chase a JNI loading issue.
tasks.matching { it.name.contains("verify") && it.name.contains("Migration") }.configureEach {
    enabled = false
}
