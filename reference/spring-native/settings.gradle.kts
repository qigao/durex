pluginManagement {
    includeBuild("../../build-bootstrap")
    includeBuild("../../build-logic")
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

plugins {
    id("durex.settings")
}

durexSettings {
    repositoryRoot.set(file("../.."))
    moduleDiscovery.set(false)
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

rootProject.name = "spring-native-reference"
