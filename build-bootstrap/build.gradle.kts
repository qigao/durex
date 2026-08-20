plugins {
    `groovy-gradle-plugin`
}

repositories {
    gradlePluginPortal()
    mavenCentral()
}

dependencies {
    implementation("org.tomlj:tomlj:1.1.1")
}

gradlePlugin {
    plugins {
        create("durexSettings") {
            id = "durex.settings"
            implementationClass = "com.github.durex.gradle.settings.DurexSettingsPlugin"
        }
        create("durexBuildLogicSettings") {
            id = "durex.build-logic-settings"
            implementationClass = "com.github.durex.gradle.settings.DurexBuildLogicSettingsPlugin"
        }
        create("durexBuildLogic") {
            id = "durex.build-logic"
            implementationClass = "com.github.durex.gradle.settings.DurexBuildLogicPlugin"
        }
    }
}
