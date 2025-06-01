
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.dagger.hilt.android")
    id("org.jetbrains.kotlin.kapt")
    id("com.google.gms.google-services")
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

        testInstrumentationRunner = "com.mgsanlet.cheftube.test.CustomTestRunner"
        
        // Configuración para pruebas
        testInstrumentationRunnerArguments["clearPackageData"] = "true"
        
        // Incluir el manifiesto de prueba
        testApplicationId = "com.mgsanlet.cheftube.test"
    }
    
    // Configuración para pruebas
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
        animationsDisabled = true
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
        freeCompilerArgs += "-opt-in=kotlin.RequiresOptIn"
    }
    buildFeatures {
        viewBinding = true
        dataBinding = true
    }
    testOptions {
        unitTests.all {
            it.jvmArgs(
                "--add-opens=java.base/java.lang=ALL-UNNAMED",
                "--add-opens=java.base/java.util=ALL-UNNAMED",
                "-XX:+EnableDynamicAgentLoading",
                "-Djdk.instrument.traceUsage=false",
                "-Dnet.bytebuddy.experimental=true"
            )
            it.systemProperty("jdk.attach.allowAttachSelf", "true")
        }
    }
}

dependencies {
    // Kotlin
    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlinx.coroutines.android)

    // Hilt
    implementation(libs.hilt.android)
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")
    kapt("com.google.dagger:hilt-android-compiler:2.56.1")
    kapt(libs.hilt.compiler)
    implementation("androidx.hilt:hilt-work:1.1.0")

    // Android core
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.core:core-splashscreen:1.0.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.activity:activity-ktx:1.8.2")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.annotation:annotation:1.7.0")
    implementation("androidx.work:work-runtime-ktx:2.9.0")

    // Architecture Components
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0")
    implementation("androidx.activity:activity-ktx:1.8.2")
    implementation("androidx.fragment:fragment-ktx:1.6.2")

    // Database
    implementation("androidx.room:room-ktx:2.6.1")

    // Network
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.logging.interceptor)
    implementation(libs.volley)
    implementation(libs.gson)

    // Image Loading
    implementation(libs.glide)
    implementation("com.github.yalantis:ucrop:2.2.8")

    // Data Binding
    implementation("androidx.databinding:databinding-runtime:8.2.2")

    // Media
    implementation("androidx.media3:media3-common-ktx:1.6.0")

    // Barcode Scanner
    implementation(libs.zxing.android.embedded)

    // Language
    implementation(libs.lingver)

    // Testing
    testImplementation(libs.junit)
    testImplementation("io.mockk:mockk:1.14.2")
    testImplementation("io.mockk:mockk-agent-jvm:1.13.3")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation("app.cash.turbine:turbine:1.0.0")
    testImplementation("com.google.truth:truth:1.1.5")
    testImplementation("androidx.arch.core:core-testing:2.2.0")

    //Firebase
    implementation(platform("com.google.firebase:firebase-bom:33.13.0"))
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-storage-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.9.0")

    //Views
    implementation("com.google.android.flexbox:flexbox:3.0.0")

    //Stat charts
    implementation ("com.github.PhilJay:MPAndroidChart:v3.1.0")

}

kapt {
    correctErrorTypes = true
    javacOptions {
        option("--add-exports=jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED")
        option("--add-exports=jdk.compiler/com.sun.tools.javac.file=ALL-UNNAMED")
        option("--add-exports=jdk.compiler/com.sun.tools.javac.main=ALL-UNNAMED")
        option("--add-exports=jdk.compiler/com.sun.tools.javac.processing=ALL-UNNAMED")
    }
}
