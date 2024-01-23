# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
-keepclassmembers class fqcn.of.javascript.interface.for.webview {
   public *;
}

# Uncomment this to preserve the line number information for
# debugging stack traces.
-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
-renamesourcefileattribute SourceFile
-keep class com.rdapps.gamepad.** { *; }
-keep class com.erz.joysticklibrary.** { *; }
-keep class androidx.appcompat.app.** { *; }
-keep class com.synnapps.carouselview.** { *; }

# Please add these rules to your existing keep rules in order to suppress warnings.
# This is generated automatically by the Android Gradle plugin.
-dontwarn kotlin.KotlinNullPointerException
-dontwarn kotlin.Metadata
-dontwarn kotlin.Result$Companion
-dontwarn kotlin.Result
-dontwarn kotlin.ResultKt
-dontwarn kotlin.coroutines.Continuation
-dontwarn kotlin.jvm.functions.Function1
-dontwarn kotlin.jvm.internal.Intrinsics
-dontwarn kotlin.jvm.internal.Lambda
-dontwarn kotlinx.coroutines.CancellableContinuation
-dontwarn org.jetbrains.annotations.NotNull