plugins {
    kotlin("jvm") version "2.2.0"
    kotlin("plugin.serialization") version "2.2.0"
    id("io.ktor.plugin") version "3.5.2"
    application
}

group = "com.studentplanner"
version = "0.0.1"

application {
    mainClass.set("com.studentplanner.ApplicationKt")
}

repositories {
    mavenCentral()
}

dependencies {
    // --- Ktor server (this is what turns Kotlin into a web/API server) ---
    implementation("io.ktor:ktor-server-core-jvm:3.5.2")
    implementation("io.ktor:ktor-server-netty-jvm:3.5.2")
    implementation("io.ktor:ktor-server-content-negotiation-jvm:3.5.2")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:3.5.2")
    implementation("io.ktor:ktor-server-call-logging-jvm:3.5.2")
    implementation("io.ktor:ktor-server-status-pages-jvm:3.5.2")

    // --- MongoDB official Kotlin coroutine driver ---
    implementation("org.mongodb:mongodb-driver-kotlin-coroutine:5.1.1")
    implementation("org.mongodb:bson-kotlinx:5.1.1")

    // --- Logging ---
    implementation("ch.qos.logback:logback-classic:1.4.14")

    // --- Testing ---
    testImplementation("io.ktor:ktor-server-test-host-jvm:3.5.2")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:2.2.0")
}

kotlin {
    jvmToolchain(17)
}
