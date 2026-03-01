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

# Retrofit/Gson Models
-keep class cn.zhzgo.study.data.** { *; }
-keep interface cn.zhzgo.study.network.** { *; }
-keep class cn.zhzgo.study.network.ApiService { *; }

# Markwon optional dependencies
-dontwarn com.caverock.androidsvg.**
-dontwarn org.commonmark.ext.gfm.strikethrough.**
-dontwarn pl.droidsonroids.gif.**

# =======================================================
# Common Third-Party Libraries ProGuard Rules
# =======================================================

# 1. Gson
-keepattributes Signature
-keepattributes *Annotation*
-keep class sun.misc.Unsafe { *; }
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# 2. OkHttp & Retrofit
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes *Annotation*
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn okhttp3.**
-dontwarn okhttp3.logging.**
-dontwarn okio.**
-keep class retrofit2.** { *; }
-dontwarn retrofit2.**
-keepclassmembers interface * {
    @retrofit2.http.* <methods>;
}

# 3. Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepnames class kotlinx.coroutines.android.AndroidExceptionPreHandler {}
-keepnames class kotlinx.coroutines.android.AndroidDispatcherFactory {}
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}
-keep class kotlin.coroutines.Continuation { *; }

# 4. AndroidX / Jetpack (Compose, Room, ViewModel)
-keep class androidx.lifecycle.** { *; }
-keep class androidx.room.** { *; }
-dontwarn androidx.room.**
-keep class androidx.compose.** { *; }

# 5. Coil (Image Loading)
-keep class coil.** { *; }

# 6. Markwon (Markdown rendering)
-keep class io.noties.markwon.** { *; }

# 7. Tencent QQ SDK
-keep class com.tencent.** { *; }
-dontwarn com.tencent.**

# Application and Activities — prevent R8 from renaming
-keep class cn.zhzgo.study.MyApplication { *; }
-keep class cn.zhzgo.study.MainActivity { *; }
-keep class cn.zhzgo.study.** extends android.app.Activity { *; }
-keep class cn.zhzgo.study.** extends androidx.activity.ComponentActivity { *; }

# 8. ML Kit
-keep class com.google.mlkit.** { *; }
-dontwarn com.google.mlkit.**

# 9. FFmpegKit
-keep class com.arthenica.ffmpegkit.** { *; }
-dontwarn com.arthenica.ffmpegkit.**