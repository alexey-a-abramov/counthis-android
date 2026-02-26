plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

import java.util.Properties

val versionPropertiesFile = rootProject.file("version.properties")
val versionProperties = Properties().apply {
    if (versionPropertiesFile.exists()) {
        versionPropertiesFile.inputStream().use { load(it) }
    } else {
        setProperty("VERSION_CODE", "1")
        setProperty("VERSION_NAME", "1.0")
    }
}

fun currentVersionCode(): Int = versionProperties.getProperty("VERSION_CODE", "1").toInt()
fun currentVersionName(): String = currentVersionCode().toString()

val shouldIncrementVersion = gradle.startParameter.taskNames.any { taskName ->
    val normalized = taskName.lowercase()
    normalized.contains("build") ||
        normalized.contains("assemble") ||
        normalized.contains("bundle") ||
        normalized.contains("install")
}

if (shouldIncrementVersion) {
    val nextVersionCode = currentVersionCode() + 1
    versionProperties.setProperty("VERSION_CODE", nextVersionCode.toString())
    versionProperties.setProperty("VERSION_NAME", nextVersionCode.toString())
    versionPropertiesFile.outputStream().use {
        versionProperties.store(it, null)
    }
    println("Incremented VERSION_CODE to $nextVersionCode")
}

android {
    namespace = "com.countthis.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.countthis.app"
        minSdk = 24
        targetSdk = 34
        versionCode = currentVersionCode()
        versionName = currentVersionName()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.preference:preference-ktx:1.2.1")

    // Statistics and Charts
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}
