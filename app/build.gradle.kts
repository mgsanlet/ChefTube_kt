plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.mgsanlet.cheftube"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.mgsanlet.cheftube"
        minSdk = 24
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.annotation)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation("com.github.bumptech.glide:glide:4.15.1")
    implementation("com.android.volley:volley:1.2.1")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation ("com.journeyapps:zxing-android-embedded:4.3.0")
    //implementation("com.google.mlkit:barcode-scanning:17.0.0")
    //implementation("androidx.camera:camera-core:1.0.0")
    //implementation("androidx.camera:camera-camera2:1.0.0")
    //implementation("androidx.camera:camera-lifecycle:1.0.0")
    //implementation("androidx.camera:camera-view:1.0.0")

}