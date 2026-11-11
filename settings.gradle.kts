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
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

val sunAiSdkSource = providers.gradleProperty("sunAiSdkSource")
    .orElse("mavenCentral")
    .get()
require(sunAiSdkSource in setOf("includeBuild", "mavenLocal", "mavenCentral")) {
    "Unsupported -PsunAiSdkSource='$sunAiSdkSource'. Use includeBuild, mavenLocal, or mavenCentral."
}
val sunAiSdkDir = providers.gradleProperty("sunAiSdkDir")
    .orElse("../SunMobileAiAgentSdk")
    .get()

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        if (sunAiSdkSource == "mavenLocal") {
            mavenLocal {
                content {
                    includeGroup("io.github.sundeepkmallick")
                }
            }
        }
        mavenCentral()
    }
}

rootProject.name = "SunCommerzAssociate"
include(":app")

if (sunAiSdkSource == "includeBuild") {
    // Gradle substitutes every SDK project matching group:name coordinates.
    includeBuild(sunAiSdkDir)
}
