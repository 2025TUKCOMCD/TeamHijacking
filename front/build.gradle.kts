// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.androidApplication) apply false
    //alias(libs.plugins.jetbrainsKotlinAndroid) apply false
    id("org.jetbrains.kotlin.android") version "1.9.25" apply false
}

/*
allprojects {
    repositories {
        google()
        mavenCentral()
        maven { url "https://devrepo.kakao.com/nexus/content/groups/public/" }
    }
}*/