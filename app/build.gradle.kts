import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    id("com.google.gms.google-services")
    kotlin("kapt")
    kotlin("plugin.serialization") version "1.9.10"
}

android {
    namespace = "com.example.chatterplay"
    compileSdk = 34

    val properties = gradleLocalProperties(rootDir, project.providers)
    val supabaseUrl: String = properties.getProperty("supabaseUrl") ?: ""
    val supabaseKey: String = properties.getProperty("supabaseKey") ?: ""

    defaultConfig {
        applicationId = "com.example.chatterplay"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        buildConfigField("String", "SUPABASE_URL", "\"$supabaseUrl\"")
        buildConfigField("String", "SUPABASE_KEY", "\"$supabaseKey\"")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.3"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Core AndroidX
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // Jetpack Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // Lifecycle & Datastore
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.datastore:datastore-preferences-android:1.1.2")
    implementation("androidx.datastore:datastore:1.1.2")

    // Room Database
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")

    // Kotlin Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")

    // Logging
    implementation("com.jakewharton.timber:timber:5.0.1")
    implementation("org.slf4j:slf4j-api:2.0.9")
    implementation("org.slf4j:slf4j-simple:2.0.9")

    // Icons & UI Components
    implementation("androidx.compose.material:material-icons-extended-android:1.7.5")
    implementation("io.github.vanpra.compose-material-dialogs:datetime:0.8.1-rc")

    // Ktor (API Calls)
    implementation("io.ktor:ktor-client-core:2.3.4")
    implementation("io.ktor:ktor-client-cio:2.3.4")
    implementation("io.ktor:ktor-client-content-negotiation:2.3.4")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.4")

    // Compose Navigation
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // Coil for Image Loading
    implementation("io.coil-kt:coil-compose:2.6.0")

    // Firebase (Using BOM)
    implementation(platform("com.google.firebase:firebase-bom:33.1.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth-ktx:23.0.0")
    implementation("com.google.firebase:firebase-firestore-ktx:25.0.0")
    implementation("com.google.firebase:firebase-storage:21.0.0")

    // Supabase
    implementation("io.github.jan-tennert.supabase:gotrue-kt:1.3.2")
    implementation("io.github.jan-tennert.supabase:postgrest-kt:1.3.2")
    implementation("io.github.jan-tennert.supabase:compose-auth:1.3.2")
    implementation("io.github.jan-tennert.supabase:compose-auth-ui:1.3.2")
    implementation("io.github.jan-tennert.supabase:storage-kt:1.3.2")

    // Accompanist Libraries
    implementation("com.google.accompanist:accompanist-permissions:0.28.0")
    implementation("com.google.accompanist:accompanist-pager:0.28.0")
    implementation("org.burnoutcrew.composereorderable:reorderable:0.9.6")

    // Compose Material 3
    implementation("androidx.compose.material3:material3:1.2.1")

    // Compose Foundation
    implementation("androidx.compose.foundation:foundation:1.6.8")
    implementation("androidx.compose.runtime:runtime:1.6.8")
    implementation("androidx.compose.foundation:foundation-layout:1.6.8")
    implementation("androidx.compose.ui:ui:1.6.8")
}
