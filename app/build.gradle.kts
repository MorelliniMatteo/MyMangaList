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
        dataBinding = true // Attiviamo il data binding
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.3" // Versione compatibile di Compose
    }

    packaging {
        resources {
            excludes.add("/META-INF/{AL2.0,LGPL2.1}")
        }
    }
}

configurations.all {
    resolutionStrategy {
        // Forziamo versioni aggiornate per evitare conflitti di dipendenza
        force("androidx.core:core-ktx:1.13.1")
        force("androidx.room:room-runtime:2.6.1")
    }
}

dependencies {
    // Base di Android
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")

    // Data Binding
    implementation("androidx.databinding:databinding-adapters:8.7.1")

    // Google Maps e Location Services
    implementation("com.google.android.gms:play-services-maps:19.0.0")
    implementation("com.google.android.gms:play-services-location:21.0.1")

    // Room (persistenza locale)
    val roomVersion = "2.6.1"
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")

    // Jetpack Compose (utilizzo del BOM per la gestione delle versioni)
    implementation(platform("androidx.compose:compose-bom:2024.10.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.navigation:navigation-compose:2.8.3")

    // Lifecycle (gestione del ciclo di vita)
    val lifecycleVersion = "2.8.6"
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:$lifecycleVersion")

    // Google Maps Compose
    implementation("com.google.maps.android:maps-compose:2.11.0")

    // Google Places
    implementation("com.google.android.libraries.places:places:3.1.0")

    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.10.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    // Altri componenti
    implementation("org.chromium.net:cronet-embedded:119.6045.31")
    implementation(kotlin("script-runtime"))
    implementation("io.coil-kt:coil-compose:2.0.0")
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
    implementation("androidx.datastore:datastore-preferences:1.1.1")
}
