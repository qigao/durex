pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
    versionCatalogs {
        create("dbLibs") {
            from(files("../gradle/versions/database.versions.toml"))
        }
    }
}

rootProject.name = "durex-build-logic"
