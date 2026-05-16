plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.helios.wear"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.helios.wear"
        minSdk = 30
        targetSdk = 34
        versionCode = 1
        versionName = "0.1.0"
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
    }
}

val composeBom = platform("androidx.compose:compose-bom:2024.12.00")

dependencies {
    // Compose
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.foundation:foundation")
    debugImplementation("androidx.compose.ui:ui-tooling")

    // Wear OS Compose
    implementation("androidx.wear.compose:compose-material3:1.0.0")
    implementation("androidx.wear.compose:compose-foundation:1.0.0")
    implementation("androidx.wear.compose:compose-navigation:1.0.0")

    // Wear OS core
    implementation("androidx.wear:wear:1.3.0")
    compileOnly("com.google.android.wearable:wearable:2.9.0")

    // Horologist (Wear OS companion libraries)
    implementation("com.google.android.horologist:horologist-compose-tools:0.6.20")
    implementation("com.google.android.horologist:horologist-tiles:0.6.20")
    implementation("com.google.android.horologist:horologist-complications-data:0.6.20")

    // Tiles & Complications
    implementation("androidx.wear.tiles:tiles-material:1.4.1")
    implementation("androidx.wear.tiles:tiles-renderer:1.4.1")
    implementation("androidx.wear.watchface:watchface-complications-data:1.2.1")
    implementation("androidx.wear.watchface:watchface-complications-data-source:1.2.1")

    // Activity & Core
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")

    // Wear-specific navigation
    implementation("androidx.navigation:navigation-compose:2.8.5")
}
