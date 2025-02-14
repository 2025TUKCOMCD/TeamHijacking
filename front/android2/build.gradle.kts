import java.util.Properties

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
}

android {
    namespace = "com.example.front"
    compileSdk = 34

    //local.properties에서 KEY 가져오기
    val localProperties = Properties().apply {
        load(project.rootProject.file("local.properties").inputStream())
    }
    val kakao_native_api_key = localProperties.getProperty("KAKAO_NATIVE_API_KEY", "")

    defaultConfig {
        applicationId = "com.example.front"
        minSdk = 30
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // BuildConfig 필드에 API 키 추가
        buildConfigField("String", "kakao_native_api_key", "\"$kakao_native_api_key\"")
    }
    buildFeatures {
        compose = true
        viewBinding = true
        dataBinding = true
        buildConfig = true
    }
    buildTypes {
        debug {
            buildConfigField("String", "kakao_native_api_key",
                localProperties["kakao_native_api_key"].toString()
            )
        }
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
        kotlinCompilerExtensionVersion = "1.5.2"
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
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation("androidx.compose.runtime:runtime:1.5.2")
    implementation("com.google.android.gms:play-services-location:21.0.1")
    implementation ("com.google.android.gms:play-services-wearable:18.1.0") // 데이터 레이블 사용 가능하게 해주는 코드
    implementation (libs.v2.user) // 카카오 로그인
}