plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

android {
    namespace = "cn.zhzgo.study"
    compileSdk = 36

    defaultConfig {
        applicationId = "cn.zhzgo.study"
        minSdk = 24
        targetSdk = 36
        versionCode = 105
        versionName = "1.0.5"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        ndk {
            // Include only the most common architectures, drop x86 and x86_64 to save large amounts of space (especially for ffmpeg-kit)
            abiFilters.addAll(listOf("armeabi-v7a", "arm64-v8a"))
        }
        multiDexEnabled = true
    }
    
    androidResources {
        localeFilters += listOf("en", "zh")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.10"
    }
    buildToolsVersion = "36.0.0"

    packaging {
        jniLibs {
            useLegacyPackaging = true
        }
    }

    @Suppress("DEPRECATION")
    useLibrary("org.apache.http.legacy")
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation("androidx.navigation:navigation-compose:2.8.0-beta02")
    implementation(libs.squareup.retrofit)
    implementation(libs.squareup.retrofit.gson)
    implementation(libs.squareup.okhttp.logging)
    implementation(libs.androidx.datastore.preferences)
    implementation("androidx.compose.material:material-icons-extended:1.6.3")
    implementation("androidx.browser:browser:1.8.0")
    implementation(libs.coil.compose)
    implementation("io.noties.markwon:core:4.6.2")
    implementation("io.noties.markwon:html:4.6.2")
    implementation("io.noties.markwon:image:4.6.2")
    implementation("io.noties.markwon:inline-parser:4.6.2")
    implementation("io.noties.markwon:ext-latex:4.6.2")
    
    // QQ Open SDK
    implementation("com.tencent.tauth:qqopensdk:3.52.0")
    
    
    // ML Kit Subject Segmentation for Background Removal
    implementation("com.google.android.gms:play-services-mlkit-subject-segmentation:16.0.0-beta1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")

    // Markdown Rendering via Markwon (AndroidView)
    implementation("io.noties.markwon:core:4.6.2")
    
    // FFmpeg Kit Full GPL for Video/Audio Process (includes libmp3lame and libx264)
    implementation("com.arthenica:ffmpeg-kit-full-gpl:6.0-2")
    
    // Room
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)
    
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    
    // Core library desugaring
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")
}