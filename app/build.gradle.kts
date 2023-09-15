plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.scaredeer.intervalreload"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.scaredeer.intervalreload"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "20230915"
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
        dataBinding = true
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.9.0")

    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2") // ViewModel
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.2") // LiveData
}