plugins {
    `groovy-gradle-plugin`
    id("durex.internal.build-logic")
}

repositories {
    gradlePluginPortal()
    mavenCentral()
}

gradlePlugin {
    plugins {
        create("durexCatalog") {
            id = "durex.internal.catalog"
            implementationClass = "com.github.durex.gradle.catalog.DurexCatalogPlugin"
        }
        create("durexModule") {
            id = "durex.module"
            implementationClass = "com.github.durex.gradle.DurexModulePlugin"
        }
        create("durexFixtureCapability") {
            id = "durex.internal.fixture"
            implementationClass = "com.github.durex.gradle.internaltesting.FixtureCapabilityPlugin"
        }
    }
}
