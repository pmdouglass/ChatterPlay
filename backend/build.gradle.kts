import jdk.tools.jlink.resources.plugins

plugins {
    kotlin("jvm") version "1.9.10"  // ✅ Kotlin JVM plugin for backend
    kotlin("plugin.serialization") version "1.9.10"  // ✅ Required for JSON serialization
    application  // ✅ Enables easy application running
}

java {
    sourceCompatibility = JavaVersion.VERSION_17  // ✅ Java 17
    targetCompatibility = JavaVersion.VERSION_17
}

repositories {
    mavenCentral()  // ✅ Required to fetch dependencies
}

dependencies {
    implementation("io.ktor:ktor-server-core-jvm:2.3.4")  // ✅ Ktor server core
    implementation("io.ktor:ktor-server-netty:2.3.4")  // ✅ Ktor Netty server
    implementation("io.ktor:ktor-server-content-negotiation:2.3.4")  // ✅ JSON negotiation
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.4")  // ✅ JSON serialization
    implementation("ch.qos.logback:logback-classic:1.2.11")  // ✅ Logging
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")  // ✅ Serialization

    testImplementation("io.ktor:ktor-server-tests-jvm:2.3.4")
    testImplementation("org.jetbrains.kotlin:kotlin-test")
}

application {
    mainClass.set("com.example.backend.MainKt")  // ✅ Entry point for your backend
}

tasks.withType<Jar> {
    manifest {
        attributes["Main-Class"] = "com.example.backend.MainKt"
    }
}
