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
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS) // settings 저장소 우선
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io") { // JitPack 저장소 추가
            name = "jitpack" // 저장소 이름 (선택적)
        }
        maven(url = "https://devrepo.kakao.com/nexus/content/groups/public/") { // 카카오 저장소 추가
            name = "kakao" // 저장소 이름 (선택적)
        }
    }
}
rootProject.name = "front"
include(":app")
include(":android2")
