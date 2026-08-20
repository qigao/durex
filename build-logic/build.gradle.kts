plugins {
    `groovy-gradle-plugin`
    id("durex.build-logic")
}

repositories {
    gradlePluginPortal()
    mavenCentral()
}

gradlePlugin {
    plugins {
        create("durexModule") {
            id = "durex.module"
            implementationClass = "com.github.durex.gradle.DurexModulePlugin"
        }
    }
}
