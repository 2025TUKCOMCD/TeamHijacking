import java.util.Properties

plugins {
    alias(libs.plugins.androidApplication) // 앱 모듈 plugin
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
    val Public_Bus_APIKEY = localProperties.getProperty("Public_Bus_APIKEY", "")
    val Public_Subway_APIKEY = localProperties.getProperty("Public_Subway_APIKEY", "")
    val Host_URL = localProperties.getProperty("Host_URL", "")
    // SmartThings OAuth 관련 정보 (application.properties 또는 local.properties에서 가져온다고 가정)

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
        buildConfigField("String", "Public_Bus_APIKEY", "\"$Public_Bus_APIKEY\"")
        buildConfigField("String", "Public_Subway_APIKEY", "\"$Public_Subway_APIKEY\"")
        buildConfigField("String", "Host_URL", "\"$Host_URL\"")
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

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        compose = true
        viewBinding = true
        dataBinding = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.15"
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
//    implementation(libs.compose.material)
    implementation(libs.compose.material3)
//    implementation(libs.compose.foundation)

    // 🔽 AndroidX Activity (compileSdk 34에 맞게 조정)
    implementation("androidx.activity:activity-ktx:1.9.3")
    implementation(libs.activity.compose)

    implementation(libs.core.splashscreen)
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.0")

    // ✅ networking & JSON/XML 처리
    implementation("com.google.code.gson:gson:2.11.0")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("org.simpleframework:simple-xml:2.7.1")
    implementation("com.squareup.retrofit2:converter-simplexml:2.9.0")

    // ✅ 추가 라이브러리
    implementation("com.github.skydoves:powerspinner:1.2.7")
    //implementation("androidx.compose.ui:ui-test-android:1.6.7")
    //implementation("androidx.compose.foundation:foundation-android:1.6.7")

    implementation("com.google.firebase:firebase-crashlytics-buildtools:3.0.2")

    //watch, app간 데이터 전송
    implementation("com.google.android.gms:play-services-wearable:18.1.0")


    // ✅ life cycle 및 뷰모델
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.fragment.ktx)
    implementation(libs.compose.material3)
    implementation(libs.compose.material)
    //implementation(project(":android2")) //Wear OS 전용 UI(곡선 리스트, BoxInsetLayout 등) 사용 가능

    // ✅ 테스트 라이브러리
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.6.0")
    debugImplementation(libs.ui.tooling)
    debugImplementation(libs.ui.test.manifest)
}
