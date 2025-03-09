plugins {
    kotlin("jvm") version "1.9.10"
    id("application")
}

group = "com.example.backend"
version = "1.0"

dependencies {
    implementation("io.ktor:ktor-server-core:2.3.4")
    implementation("io.ktor:ktor-server-netty:2.3.4")
    implementation("io.ktor:ktor-server-content-negotiation:2.3.4")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.4")

    implementation("ch.qos.logback:logback-classic:1.2.11")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

    testImplementation("io.ktor:ktor-server-tests:2.3.4")
    testImplementation("org.jetbrains.kotlin:kotlin-test:1.9.10")
}

kotlin {
    jvmToolchain(17)
}

application {
    mainClass.set("com.example.backend.UpdateAlert")
}

tasks.withType<JavaCompile> {
    sourceCompatibility = JavaVersion.VERSION_17.toString()
    targetCompatibility = JavaVersion.VERSION_17.toString()
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
}
