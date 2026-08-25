pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

plugins {
    id("io.github.qigao.simpledsl.settings") version "0.2.0"
}

simpledslSettings {
    repositoryRoot.set(layout.settingsDirectory.dir("../.."))
    moduleDiscovery.set(false)
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

rootProject.name = "spring-capabilities"
