import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "2.0.21"
    kotlin("plugin.serialization") version "2.0.21"
    id("io.ktor.plugin") version "3.0.1"
    application
}

group = "com.engblog"
version = "0.1.0"

application {
    mainClass.set("com.engblog.ApplicationKt")
}

repositories {
    mavenCentral()
}

val exposedVersion = "0.55.0"

dependencies {
    // Ktor server
    implementation("io.ktor:ktor-server-core")
    implementation("io.ktor:ktor-server-netty")
    implementation("io.ktor:ktor-server-content-negotiation")
    implementation("io.ktor:ktor-serialization-kotlinx-json")
    implementation("io.ktor:ktor-server-call-logging")
    implementation("io.ktor:ktor-server-status-pages")

    // Ktor client (feed polling + Claude API calls)
    implementation("io.ktor:ktor-client-core")
    implementation("io.ktor:ktor-client-cio")
    implementation("io.ktor:ktor-client-content-negotiation")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")

    // RSS/Atom parsing
    implementation("com.rometools:rome:2.1.0")

    // Postgres access
    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-java-time:$exposedVersion")
    implementation("org.postgresql:postgresql:42.7.4")
    implementation("com.zaxxer:HikariCP:5.1.0")

    // Logging
    implementation("ch.qos.logback:logback-classic:1.5.8")

    testImplementation("io.ktor:ktor-server-test-host")
    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(17)
}

tasks.withType<Test> {
    useJUnitPlatform()
}

// Manual Phase 1 test runner: ./gradlew ingest -Psummarize
tasks.register<JavaExec>("ingest") {
    group = "application"
    description = "Polls all configured feeds and stores new posts. Pass -Psummarize to also call the Claude API."
    mainClass.set("com.engblog.IngestCliKt")
    classpath = sourceSets["main"].runtimeClasspath
    args = if (project.hasProperty("summarize")) listOf("--summarize") else emptyList()
}
