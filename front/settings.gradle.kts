pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
    plugins {
        id("com.android.application") version "8.4.0" apply false
        id("com.android.library") version "8.4.0" apply false
        id("org.jetbrains.kotlin.android") version "1.9.24" apply false // Kotlin도 최신
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS) // settings 저장소 우선
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io") {
            name = "jitpack" // JitPack 저장소 추가
        }
        maven(url = "https://devrepo.kakao.com/nexus/content/groups/public/") {
            name = "kakao" // 카카오 저장소 추가
        }
    }
}

rootProject.name = "front"
include(":app")
include(":android2")
