import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties
import java.util.Properties

plugins {
    alias(libs.plugins.androidApplication)
    //alias(libs.plugins.jetbrainsKotlinAndroid)
    id("org.jetbrains.kotlin.android")
    //id("org.jetbrains.kotlin.android") version "1.9.0" apply false

    //id("com.android.application") version "8.7.3" apply false
}

android {
    namespace = "com.example.front"
    compileSdk = 34

    // localProperties 파일에서 변수 지정 및 파일 정보 받아오기
    val localProperties = Properties()
    localProperties.load(project.rootProject.file("local.properties").inputStream())

    // 기존 API 키와 함께 SMARTTHINGS_API_TOKEN 추가
    val ODsay_APIKEY = localProperties.getProperty("ODsay_APIKEY") ?: ""
    val Geolocation_APIKEY = localProperties.getProperty("Geolocation_APIKEY") ?: ""
    val SMARTTHINGS_API_TOKEN = localProperties.getProperty("SMARTTHINGS_API_TOKEN") ?: ""

    defaultConfig {
        applicationId = "com.example.front"
        minSdk = 30
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        vectorDrawables {
            useSupportLibrary = true
        }

        // buildConfigField에 API 키 추가
        buildConfigField("String", "ODsay_APIKEY", "\"$ODsay_APIKEY\"")
        buildConfigField("String", "Geolocation_APIKEY", "\"$Geolocation_APIKEY\"")
        buildConfigField("String", "SMARTTHINGS_API_TOKEN", "\"$SMARTTHINGS_API_TOKEN\"")
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
        buildConfig = true
        viewBinding = true
        dataBinding = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    //위도 경도를 위한 google-gecodingAPI
    implementation ("com.google.maps:google-maps-services:2.0.0")
    //위치를 받아오는 코드
    implementation("com.google.android.gms:play-services-location:21.0.1")
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
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.ui.test.junit4)
    debugImplementation(libs.ui.tooling)
    debugImplementation(libs.ui.test.manifest)
    implementation ("com.google.code.gson:gson:2.11.0")
    implementation ("com.squareup.retrofit2:retrofit:2.11.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation ("com.squareup.okhttp3:okhttp:4.12.0")
    implementation ("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation ("com.github.skydoves:powerspinner:1.2.7")
}