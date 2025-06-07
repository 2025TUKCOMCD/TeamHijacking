import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
}

android {
    namespace = "com.example.front"
    compileSdk = 34
    //뭔가뭔가
    // local.properties 에서 API 키와 호스트 URL 을 가져옴
    val localProperties = Properties().apply {
        // 중요한 변경: 'front' 디렉토리 바로 아래에 있는 local.properties를 읽도록 경로를 명확히 지정합니다.
        // project.rootProject는 Git 저장소의 가장 상위 디렉토리(TeamHijacking/)를 나타냅니다.
        // 그 아래에 "front/local.properties"가 있으므로, 이 경로를 사용합니다.
        val localPropertiesFile = project.rootProject.file("front/local.properties")

        if (localPropertiesFile.exists()) {
            load(FileInputStream(localPropertiesFile))
        } else {
            // 파일이 없는 경우 경고만 출력하고 빌드가 실패하지 않도록 처리합니다.
            println("WARNING: local.properties file not found at ${localPropertiesFile.absolutePath}. Using empty strings for API keys.")
        }
    }                                  //KAKAO_NATIVE_API_KEY    cf939a3f6eb2a3a0c85cce072098dba2
    val KAKAO_NATIVE_API_KEY = localProperties.getProperty("KAKAO_NATIVE_API_KEY", "cf939a3f6eb2a3a0c85cce072098dba2")
    val HOST_URL = localProperties.getProperty("Host_URL", "")

    //로컬 프로퍼티가 가져온게 제대로 됐는지 확인
    //발급받은 걸 제대로 설정했는지 확인하기...
    //서버 접속하고 설정할 때
    //포스트에 배리어 키.... <? 스마트띵스 api 키 중에서.,. 헤더에 앱 키를 보통 넣음..
    //헤더에서 보통 체크를 하니까... 그걸 테스트

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
        kotlinCompilerExtensionVersion = "1.5.15"
    }

    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    implementation(platform(libs.compose.bom))

    implementation(libs.ui) // androidx.compose.ui:ui
    implementation(libs.ui.tooling.preview) // androidx.compose.ui:ui-tooling-preview
    implementation(libs.activity.compose) // activity-compose
    // implementation(libs.compose.runtime) // 일반적으로 ui 라이브러리가 transitively 가져오므로 명시적으로 추가할 필요는 없지만, 문제가 지속되면 추가 고려
    // --- Compose 라이브러리 추가 끝 ---


    implementation(libs.core.ktx)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.play.services.wearable)
    implementation(libs.compose.material3)
    testImplementation(libs.junit)
    // 카카오 로그인 용
    implementation(libs.v2.user)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation("com.google.android.gms:play-services-location:21.0.1")
    // JSON, XML 처리용
    implementation("com.squareup.retrofit2:retrofit:2.9.0")  // API 처리용, Retrofit 사용 위해 추가 해야 한다고 함
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.google.code.gson:gson:2.11.0") // 혹시 모르니 넣어봄
}
