import java.util.Properties

plugins {
    alias(libs.plugins.androidApplication)
    id("org.jetbrains.kotlin.android") version "1.9.25"
}

android {
    namespace = "com.example.front"
    compileSdk = 34

    // local.properties 에서 API 키와 호스트 URL 을 가져옴
    val localProperties = Properties().apply {
        load(project.rootProject.file("local.properties").inputStream())
    }
    val KAKAO_NATIVE_API_KEY = localProperties.getProperty("KAKAO_NATIVE_API_KEY", "")
    val HOST_URL = localProperties.getProperty("Host_URL", "")

    defaultConfig {
        minSdk = 30
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // BuildConfig 필드에 API 키 추가
        buildConfigField("String", "KAKAO_NATIVE_API_KEY", "\"$KAKAO_NATIVE_API_KEY\"")
        buildConfigField("String", "HOST_URL", "\"$HOST_URL\"")
        manifestPlaceholders["KAKAO_NATIVE_API_KEY"] = KAKAO_NATIVE_API_KEY
        manifestPlaceholders["HOST_URL"] = HOST_URL
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
        kotlinCompilerExtensionVersion = "1.5.12"
    }

    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    implementation(platform(libs.compose.bom))
    implementation(libs.core.ktx)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.play.services.wearable)
    testImplementation(libs.junit)
    // 카카오 로그인 용
    implementation(libs.v2.user)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation("com.google.android.gms:play-services-location:21.0.1")
    implementation("com.google.android.gms:play-services-wearable:18.1.0") // 데이터 레이블 사용 가능 하게 해주는 코드
    // JSON, XML 처리용
    implementation("com.squareup.retrofit2:retrofit:2.9.0")  // API 처리용, Retrofit 사용 위해 추가 해야 한다고 함
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.google.code.gson:gson:2.11.0") // 혹시 모르니 넣어봄
}
