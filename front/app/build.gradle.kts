import java.util.Properties

plugins {
    alias(libs.plugins.androidApplication)
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.example.front"
    compileSdk = 34

    // local.properties 파일에서 API 키 가져오기
    val localProperties = Properties().apply {
        load(project.rootProject.file("local.properties").inputStream())
    }
    val ODsay_APIKEY = localProperties.getProperty("ODsay_APIKEY", "")
    val Geolocation_APIKEY = localProperties.getProperty("Geolocation_APIKEY", "")
    val SMARTTHINGS_API_TOKEN = localProperties.getProperty("SMARTTHINGS_API_TOKEN", "")
    val Public_APIKEY = localProperties.getProperty("Public_APIKEY", "")

    defaultConfig {
        applicationId = "com.example.front"
        minSdk = 30
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        vectorDrawables.useSupportLibrary = true

        // BuildConfig 필드에 API 키 추가
        buildConfigField("String", "ODsay_APIKEY", "\"$ODsay_APIKEY\"")
        buildConfigField("String", "Geolocation_APIKEY", "\"$Geolocation_APIKEY\"")
        buildConfigField("String", "SMARTTHINGS_API_TOKEN", "\"$SMARTTHINGS_API_TOKEN\"")
        buildConfigField("String", "Public_APIKEY", "\"$Public_APIKEY\"")
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
        kotlinCompilerExtensionVersion = "1.5.1"
    }

    packaging {
        resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
}

dependencies {
    // Google Maps API
    implementation("com.google.maps:google-maps-services:2.0.0")
    // Google Play Services - Location
    implementation("com.google.android.gms:play-services-location:21.0.1")
    // AndroidX 및 Compose 관련 라이브러리
    implementation(libs.play.services.wearable)
    implementation(platform(libs.compose.bom))
    implementation(libs.ui)
    implementation(libs.ui.tooling.preview)
    implementation(libs.compose.material)
    implementation(libs.compose.foundation)
    implementation(libs.activity.compose)
    implementation(libs.core.splashscreen)
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.activity:activity:1.9.3")
    implementation("androidx.constraintlayout:constraintlayout:2.2.0")
    implementation("androidx.compose.material3:material3-android:1.3.1")

    // JSON&XML 처리 및 네트워킹
    implementation("com.google.code.gson:gson:2.11.0")
    implementation("com.squareup.retrofit2:retrofit:2.9.0") // 최신 버전으로 변경
    implementation("com.squareup.retrofit2:converter-gson:2.9.0") // 최신 버전으로 변경
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("org.simpleframework:simple-xml:2.7.1")
    implementation("com.squareup.retrofit2:converter-simplexml:2.9.0")

    // 추가 라이브러리
    implementation("com.github.skydoves:powerspinner:1.2.7")

    // 테스트 라이브러리
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.ui.test.junit4)
    debugImplementation(libs.ui.tooling)
    debugImplementation(libs.ui.test.manifest)
}
