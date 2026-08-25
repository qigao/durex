pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

plugins {
    id("io.github.qigao.simpledsl.settings") version "0.1.1"
}

simpledslSettings {
    repositoryRoot.set(layout.settingsDirectory.dir("../.."))
    dependencyManifest.set(layout.settingsDirectory.file("../../gradle/dependencies/durex.toml"))
    modulesManifest.set(layout.settingsDirectory.file("modules.toml"))
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

rootProject.name = "spring-native-reference"
