plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.example.btqt_nhom3"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.btqt_nhom3"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.gridlayout)
    implementation(libs.androidx.exifinterface)
    implementation(libs.googleid)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.play.services.auth)
    implementation(libs.coil)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.cardview)
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.biometric)

    implementation("com.davemorrissey.labs:subsampling-scale-image-view:3.10.0")

    implementation("com.vanniktech:android-image-cropper:4.7.0")
    // Credential Manager
//    implementation ("androidx.credentials:credentials:1.2.0")
//    implementation ("androidx.credentials:credentials-play-services-auth:1.2.0")
//    // Google Identity Services (new Sign-In)
//    implementation ("com.google.android.libraries.identity.googleid:googleid:1.1.0")
//    // Authorization API (để request quyền Google Drive)
//    implementation ("com.google.android.libraries.identity.googleid:googleid-authorization:1.1.0")
//    // OkHttp for REST API
//    implementation ("com.squareup.okhttp3:okhttp:4.12.0")
//    // JSON
//    implementation ("org.json:json:20231013")
//    implementation("com.google.android.gms:play-services-auth:21.4.0")
//    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")
}