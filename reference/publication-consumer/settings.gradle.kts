import org.gradle.api.initialization.resolve.RepositoriesMode

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        val durexRepository = providers.gradleProperty("durexRepository").orNull
            ?: error("-PdurexRepository=<staged Maven repository> is required")
        maven {
            url = uri(durexRepository)
        }
        mavenCentral()
    }
}

rootProject.name = "durex-publication-consumer"
