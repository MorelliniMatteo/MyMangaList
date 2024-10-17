plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android") version "1.9.10" // Versione Kotlin aggiornata
    id("com.google.devtools.ksp") // KSP per Room
}

android {
    namespace = "com.example.mymangalist"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.mymangalist"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true
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
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.3" // Assicurati che la versione del Compose Compiler sia compatibile con Kotlin 1.9.10
    }

    packaging {
        resources {
            excludes.add("/META-INF/{AL2.0,LGPL2.1}")
        }
    }
}

configurations.all {
    resolutionStrategy {
        force("androidx.room:room-runtime:2.6.1") // Forziamo la versione di Room
        force("androidx.compose.ui:ui:1.7.4") // Forziamo la versione di Compose
    }
}

dependencies {
    val roomVersion = "2.6.1" // Versione aggiornata di Room
    val composeVersion = "1.7.4" // Versione aggiornata di Jetpack Compose
    val lifecycleVersion = "2.8.6" // Versione aggiornata di Lifecycle
    val navigationVersion = "2.8.3" // Versione aggiornata di Navigation

    // Core e AppCompat
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")

    // Room dependencies
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")

    // Jetpack Compose Dependencies
    implementation(platform("androidx.compose:compose-bom:2024.10.00")) // BOM aggiornato per gestire le dipendenze
    implementation("androidx.compose.ui:ui:$composeVersion")
    implementation("androidx.compose.ui:ui-graphics:$composeVersion")
    implementation("androidx.compose.ui:ui-tooling-preview:$composeVersion")

    // Material Design 3
    implementation("androidx.compose.material3:material3")

    // Activity Compose
    implementation("androidx.activity:activity-compose:1.9.3")

    // Lifecycle dependencies
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:$lifecycleVersion")

    // Navigation for Jetpack Compose
    implementation("androidx.navigation:navigation-compose:$navigationVersion")

    // Optional Animation
    implementation("androidx.compose.animation:animation:$composeVersion")

    // Testing dependencies
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.10.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:$composeVersion")
    debugImplementation("androidx.compose.ui:ui-tooling:$composeVersion")
    debugImplementation("androidx.compose.ui:ui-test-manifest:$composeVersion")

    // Kotlin script runtime
    implementation(kotlin("script-runtime"))
}
