# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Compose-related classes
-keep class androidx.compose.** {*;}
-dontwarn androidx.compose.**

# Kotlin serialization classes
-keep class kotlinx.serialization.** {*;}
-dontwarn androidx.compose.**

# Firebase SDK classes
-keep class com.google.firebase.** {*;}
-dontwarn com.google.firebase.**

# Supabase SDK classes
-keep class io.github.jan.tennert.supabase.** {*;}
-dontwarn io.github.jan.tennert.supabase.**

# Keep Retrofit API Service interfaces
-keep interface * implements retrofit2.Call { *; }

# Keep Glide classes
-keep class com.bumptech.glide.** { *; }
-dontwarn com.bumptech.glide.**

# Keep Ktor client classes
-keep class io.ktor.** { *; }
-dontwarn io.ktor.**

# Keep SLF4J classes
-keep class org.slf4j.** { *; }
-dontwarn org.slf4j.**

# Preserve Retrofit annotations
-keepattributes *Annotation*
-keep class retrofit2.** { *; }
-keep class okhttp3.** { *; }
-keep class com.google.gson.** { *; }

# Prevent Gson from stripping necessary classes
-keep class com.example.chatterplay.model.** { *; }
-keep class com.example.chatterplay.network.** { *; }

# Coroutines Keep Rules
-dontwarn kotlinx.coroutines.**
-keep class kotlinx.coroutines.** { *; }

# Keep Retrofit response models
-keepclassmembers class * {
    @retrofit2.http.* <methods>;
}

# Keep Retrofit API Service Interfaces
-keep interface * implements retrofit2.Call { *; }

# Allow some debug logging
-dontnote retrofit2.Platform
-dontwarn okio.**
-dontwarn retrofit2.**
-dontwarn javax.annotation.**
