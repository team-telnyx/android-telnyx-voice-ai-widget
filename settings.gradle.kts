pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
        maven {
            name = "GitHubPackages"
            val githubProperties = java.util.Properties()
            val githubPropertiesFile = File(rootDir, "github.properties")
            if (githubPropertiesFile.exists()) {
                githubProperties.load(java.io.FileInputStream(githubPropertiesFile))
            }
            val githubRepo = githubProperties["GITHUB_REPOSITORY"] as String? ?: System.getenv("GITHUB_REPOSITORY") ?: ""
            url = uri("https://maven.pkg.github.com/$githubRepo")
            credentials {
                username = githubProperties["GITHUB_USERNAME"] as String? ?: System.getenv("GITHUB_USERNAME")
                password = githubProperties["GITHUB_TOKEN"] as String? ?: System.getenv("GITHUB_TOKEN")
            }
        }
    }
}

rootProject.name = "android-telnyx-voice-ai-widget"
include(":widget")
include(":example")