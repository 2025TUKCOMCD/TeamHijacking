import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties
import java.util.Properties

plugins {
    alias(libs.plugins.androidApplication)
    //alias(libs.plugins.jetbrainsKotlinAndroid)
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.example.front"
    compileSdk = 34
    //localProperties의 변수 지정 및 파일 정보 받아오기
    val localProperties = Properties()
    localProperties.load(project.rootProject.file("local.properties").inputStream())
    //ODsay_APIKEY이름 으로 받아옴
    val ODsay_APIKEY = localProperties.getProperty("ODsay_APIKEY")?:""

    defaultConfig {
        applicationId = "com.example.front"
        minSdk = 30
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        vectorDrawables {
            useSupportLibrary = true
        }
        //buildConfig필드에 ODsay_APIKEY로 저장
        buildConfigField("String", "ODsay_APIKEY", ODsay_APIKEY)
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            //리소스나 xml파일에서도 apikey 접근 가능하게 해줌 일단 필요 없어서 주석
           // resValue("String","APIKEY", gradleLocalProperties(rootDir,providers).getProperty("APIKEY"))

            //코드 내에서 api접근 가능하게함
           // buildConfigField("String","APIKEY", gradleLocalProperties(rootDir,providers).getProperty("APIKEY"))

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
        buildConfig = true
        compose = true
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