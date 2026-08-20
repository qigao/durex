pluginManagement {
    includeBuild("../build-bootstrap")
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

plugins {
    id("durex.build-logic-settings")
}

durexBuildLogicSettings {
    repositoryRoot.set(file(".."))
}

rootProject.name = "durex-build-logic"
