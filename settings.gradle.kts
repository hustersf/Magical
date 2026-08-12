pluginManagement {
    includeBuild("build-logic")
    repositories {
        maven { url = uri("https://maven.aliyun.com/repository/public") }
        maven { url = uri("https://maven.aliyun.com/repository/google") }
        maven { url = uri("https://maven.aliyun.com/repository/gradle-plugin") }
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
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        maven { url = uri("https://maven.aliyun.com/repository/public") }
        maven { url = uri("https://maven.aliyun.com/repository/google") }
        maven { url = uri("https://maven.aliyun.com/repository/gradle-plugin") }
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "Magical"

include(":packages:apollo")
include(":packages:diana")
include(":packages:aurora")
include(":packages:wan-android")
include(":packages:work:snapu")
include(":packages:ai-edge")

include(":framework:network")
include(":framework:download")
include(":framework:player")
include(":framework:image")
include(":framework:mlkit")

include(":core:legacy")
include(":core:webview")
include(":core:social-sdk")
include(":core:login")
include(":core:share")
include(":core:auto-play")
include(":core:ui")
include(":core:res")
include(":core:common")
include(":core:media")
include(":core:speech")

include(":core:ai-edge:data")
include(":core:ai-edge:design")
include(":core:ai-edge:database")
include(":core:ai-edge:domain")

include(":feature:ai-edge:agent:api")
include(":feature:ai-edge:agent:impl")
include(":feature:ai-edge:chat:api")
include(":feature:ai-edge:chat:impl")
include(":feature:ai-edge:meeting:api")
include(":feature:ai-edge:meeting:impl")
include(":feature:ai-edge:models:api")
include(":feature:ai-edge:models:impl")
include(":feature:ai-edge:vision:api")
include(":feature:ai-edge:vision:impl")
include(":feature:ai-edge:explore:api")
include(":feature:ai-edge:explore:impl")
