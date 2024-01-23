# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
-keepclassmembers class com.rdapps.gamepad.nintendo_switch.CustomFragment$JoyConJSInterface {
   public *;
}
-keepclassmembers class com.rdapps.gamepad.util.ControllerFunctions {
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

# see https://github.com/square/retrofit/issues/3751#issuecomment-1192043644
# TODO can be removed after a retrofit update is available
# Keep generic signature of Call, Response (R8 full mode strips signatures from non-kept items).
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response

# With R8 full mode generic signatures are stripped for classes that are not
# kept. Suspend functions are wrapped in continuations where the type argument
# is used.
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation
