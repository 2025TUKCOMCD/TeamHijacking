import java.util.Properties

plugins {
    alias(libs.plugins.androidApplication)
    //alias(libs.plugins.jetbrainsKotlinAndroid)
    id("org.jetbrains.kotlin.android") version "1.9.24"
}

android {
    namespace = "com.example.front"
    compileSdk = 34

    //local.properties에서 KEY 가져오기
    val localProperties = Properties().apply {
        load(project.rootProject.file("local.properties").inputStream())
    }
    val KAKAO_NATIVE_API_KEY = localProperties.getProperty("KAKAO_NATIVE_API_KEY", "")

    defaultConfig {
        applicationId = "com.example.front"
        minSdk = 30
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // BuildConfig 필드에 API 키 추가
        buildConfigField("String", "KAKAO_NATIVE_API_KEY", "\"$KAKAO_NATIVE_API_KEY\"")
        manifestPlaceholders["KAKAO_NATIVE_API_KEY"] = KAKAO_NATIVE_API_KEY

    }
    buildFeatures {
        compose = true
        viewBinding = true
        dataBinding = true
        buildConfig = true
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
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {

    implementation(libs.core.ktx)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.play.services.wearable)
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.activity:activity:1.9.3")
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")
    testImplementation(libs.junit)
    //implementation(libs.v2.user.v2206) sync Now 오류로 잠시 주석처리
    implementation(libs.v2.user)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation("androidx.compose.runtime:runtime:1.5.2")
    implementation("com.google.android.gms:play-services-location:21.0.1")
    implementation ("com.google.android.gms:play-services-wearable:18.1.0") // 데이터 레이블 사용 가능하게 해주는 코드
//    implementation ("libs.v2.user") // 카카오 로그인
}
