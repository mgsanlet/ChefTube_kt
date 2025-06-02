
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
    implementation(libs.androidx.hilt.navigation.compose)
    kapt(libs.hilt.android.compiler)
    kapt(libs.hilt.compiler)
    implementation(libs.androidx.hilt.work)

    // Android core
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.activity.ktx)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.annotation)
    implementation(libs.androidx.work.runtime.ktx)

    // Architecture Components
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.activity.ktx)
    implementation(libs.androidx.fragment.ktx)

    // Red
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.logging.interceptor)
    implementation(libs.volley)
    implementation(libs.gson)

    // Carga de imágenes
    implementation(libs.glide)
    implementation(libs.ucrop)

    // Data Binding
    implementation(libs.androidx.databinding.runtime)

    // Media
    implementation(libs.androidx.media3.common.ktx)

    // Escáner código de barras
    implementation(libs.zxing.android.embedded)

    // Internacionalización
    implementation(libs.lingver)

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.mockk.agent.jvm)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
    testImplementation(libs.truth)
    testImplementation(libs.androidx.core.testing)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth.ktx)
    implementation(libs.firebase.storage.ktx)
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.kotlinx.coroutines.play.services)

    // Vistas
    implementation(libs.flexbox)

    // Gráficos de estadísticas
    implementation (libs.mpandroidchart)

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
