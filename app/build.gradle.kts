plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.kapt")

    id("com.google.gms.google-services")
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "com.example.eureka"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.example.eureka"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        manifestPlaceholders["ARCORE_API_KEY"] = "AIzaSyClCq6DrEIFSfdKXOZurPfDFqkOLnaUuds"
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        compose = true
    }
    // composeOptions block not needed with Kotlin 2.x + Compose Compiler Gradle plugin
}

dependencies {

    // ── Compose + AndroidX (existing) ─────────────────────────
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // ── AR + 3D Rendering ─────────────────────────────────────
    // ⚠️ 4.x: ARSceneView is now a @Composable, not a View
    implementation("io.github.sceneview:arsceneview:4.0.1")            // conflict → new wins

    // ── Hand Tracking ─────────────────────────────────────────
    implementation("com.google.mediapipe:tasks-vision:0.10.34")        // conflict → new wins

    // ── Firebase ──────────────────────────────────────────────
    // KTX suffixes removed from BoM since v34.0.0 (July 2025)
    implementation(platform("com.google.firebase:firebase-bom:34.12.0"))
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-auth")

    // ── Coroutines ────────────────────────────────────────────
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")  // conflict → new wins
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.8.0") // existing, no conflict

    // ── ViewModel + Compose ───────────────────────────────────
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.9.0")     // new addition

    // ── Permissions ───────────────────────────────────────────
    implementation("com.google.accompanist:accompanist-permissions:0.37.3")    // new addition

    // ── Navigation ────────────────────────────────────────────
    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation("androidx.compose.material:material-icons-extended")

    // ── Testing (existing) ────────────────────────────────────
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)

    implementation("com.google.dagger:hilt-android:2.52")
}