pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

plugins {
    id("io.github.qigao.simpledsl.settings") version "0.1.0"
}

simpledslSettings {
    repositoryRoot.set(layout.settingsDirectory.dir("../.."))
    dependencyManifest.set(layout.settingsDirectory.file("../../gradle/dependencies/durex.toml"))
    moduleDiscovery.set(false)
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

rootProject.name = "spring-capabilities"
