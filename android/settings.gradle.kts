// ---------------------------------------------------------------------------
// Gradle settings - dependency resolution is centralised here (no repositories
// declared in module build files). Google first, then Maven Central.
// ---------------------------------------------------------------------------
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
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "SEARCHHH"
include(":app")

// CI builds are reproducible: the Gradle daemon is not reused across jobs.
gradle.startParameter.parallelProjectExecutionEnabled = true
