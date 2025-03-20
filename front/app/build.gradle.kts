import java.util.Properties

plugins {
    alias(libs.plugins.androidApplication)
    id("org.jetbrains.kotlin.android") version "1.9.24"
}

android {
    namespace = "com.example.front"
    compileSdk = 34

    // local.properties에서 API 키 가져오기
    val localProperties = Properties().apply {
        load(project.rootProject.file("local.properties").inputStream())
    }

    defaultConfig {
        applicationId = "com.example.front"
        minSdk = 30
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        vectorDrawables.useSupportLibrary = true

        // API 키를 BuildConfig 필드에 추가
        buildConfigField("String", "ODsay_APIKEY", "\"${localProperties.getProperty("ODsay_APIKEY", "")}\"")
        buildConfigField("String", "Geolocation_APIKEY", "\"${localProperties.getProperty("Geolocation_APIKEY", "")}\"")
        buildConfigField("String", "SMARTTHINGS_API_TOKEN", "\"${localProperties.getProperty("SMARTTHINGS_API_TOKEN", "")}\"")
        buildConfigField("String", "Public_Bus_APIKEY", "\"${localProperties.getProperty("Public_Bus_APIKEY", "")}\"")
        buildConfigField("String", "Public_Subway_APIKEY", "\"${localProperties.getProperty("Public_Subway_APIKEY", "")}\"")
        buildConfigField("String", "Host_URL", "\"${localProperties.getProperty("Host_URL", "")}\"")
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
        viewBinding = true
        dataBinding = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }

    packaging {
        resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
}

dependencies {
    // ✅ Google Maps & 위치 서비스
    implementation("com.google.maps:google-maps-services:2.2.0")
    implementation("com.google.android.gms:play-services-location:21.3.0")

    // ✅ AndroidX 및 Compose 관련 라이브러리
    implementation(libs.play.services.wearable)
    implementation(platform(libs.compose.bom))
    implementation(libs.ui)
    implementation(libs.ui.tooling.preview)
    implementation(libs.compose.material)
    implementation(libs.compose.foundation)

    // 🔽 AndroidX Activity (compileSdk 34에 맞게 조정)
    implementation("androidx.activity:activity-ktx:1.8.0")
    implementation("androidx.activity:activity:1.8.0")
    implementation("androidx.activity:activity-compose:1.8.0")

    implementation(libs.core.splashscreen)
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.10.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.compose.material3:material3-android:1.3.1")

    // ✅ 네트워킹 & JSON/XML 처리
    implementation("com.google.code.gson:gson:2.11.0")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("org.simpleframework:simple-xml:2.7.1")
    implementation("com.squareup.retrofit2:converter-simplexml:2.9.0")

    // ✅ 추가 라이브러리
    implementation("com.github.skydoves:powerspinner:1.2.7")
    implementation("androidx.compose.ui:ui-test-android:1.6.7")
    implementation("androidx.compose.foundation:foundation-android:1.6.7")
    implementation("com.google.firebase:firebase-crashlytics-buildtools:3.0.2")

    // ✅ Wear OS 관련 라이브러리
    implementation("com.google.android.gms:play-services-wearable:18.1.0")
    implementation("androidx.wear:wear:1.3.0")

    // ✅ 라이프사이클 및 뷰모델
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.fragment.ktx)

    // ✅ 테스트 라이브러리
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.ui.test.junit4)
    debugImplementation(libs.ui.tooling)
    debugImplementation(libs.ui.test.manifest)
}
