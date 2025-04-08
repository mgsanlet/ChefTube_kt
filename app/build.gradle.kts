plugins {
    alias(libs.plugins.android.application)
    id("kotlin-android")
}

android {
    namespace = "com.mgsanlet.cheftube"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.mgsanlet.cheftube"
        minSdk = 26
        targetSdk = 35
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(libs.kotlin.stdlib)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.annotation)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.room.ktx)
    implementation(libs.media3.common.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation(libs.glide)
    implementation(libs.volley)
    implementation(libs.gson)
    implementation (libs.zxing.android.embedded)
    implementation(libs.lingver)
    implementation(libs.jbcrypt)
    implementation (libs.kotlinx.coroutines.android)
    // Fragment
    implementation(libs.fragment.ktx)
    // Activity
    implementation(libs.activity.ktx)
    // ViewModel
    implementation(libs.lifecycle.viewmodel.ktx.v231)
    // LiveData
    implementation(libs.lifecycle.livedata.ktx.v231)
    //Splash
    implementation(libs.core.splashscreen)
}