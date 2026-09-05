plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    id("com.android.library")
    id("app.cash.sqldelight")
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

    // Kotlin/Native can't cross-compile Apple targets from Windows, so these are only
    // configured when Gradle actually runs on macOS — that keeps every build on this
    // Windows dev machine green while making the module build correctly for iOS the
    // moment it's opened on a Mac with Xcode installed.
    val isMacOs = org.gradle.internal.os.OperatingSystem.current().isMacOsX
    if (isMacOs) {
        listOf(iosX64(), iosArm64(), iosSimulatorArm64()).forEach { target ->
            target.binaries.framework {
                baseName = "Shared"
            }
        }
    }

    sourceSets {
        val commonMain by getting {
            kotlin.srcDir("commonMain")
            dependencies {
                // These all appear in public signatures (FeedRepository, ApiService, Post,
                // DriverFactory), so consumers like androidApp need them at compile time too.
                api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
                api("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
                api("org.jetbrains.kotlinx:kotlinx-datetime:0.6.1")
                api("io.ktor:ktor-client-core:3.0.1")
                api("app.cash.sqldelight:runtime:2.0.2")
                implementation("io.ktor:ktor-client-content-negotiation:3.0.1")
                implementation("io.ktor:ktor-serialization-kotlinx-json:3.0.1")
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

        if (isMacOs) {
            val iosMain by getting {
                kotlin.srcDir("iosMain")
                dependencies {
                    implementation("io.ktor:ktor-client-darwin:3.0.1")
                    implementation("app.cash.sqldelight:native-driver:2.0.2")
                }
            }
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
