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
# Keep Logback classes
-keep class ch.qos.logback.** { *; }
-dontwarn ch.qos.logback.**

# Keep Java Management Extensions (JMX) classes
-keep class javax.management.** { *; }
-dontwarn javax.management.**

# Keep Java Reflection classes
-keep class sun.reflect.** { *; }
-dontwarn sun.reflect.**

# Keep Java Servlet API (only if needed)
-keep class javax.servlet.** { *; }
-dontwarn javax.servlet.**

# Keep Janino (if Logback uses it)
-keep class org.codehaus.janino.** { *; }
-dontwarn org.codehaus.janino.**



