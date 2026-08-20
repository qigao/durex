pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
    }
    versionCatalogs {
        create("sLibs") {
            from(files("../../gradle/versions/spring.versions.toml"))
        }
    }
}

rootProject.name = "spring-capabilities"
