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
    }
}
