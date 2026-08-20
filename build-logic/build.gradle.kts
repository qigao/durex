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
        create("durexCatalog") {
            id = "durex.catalog"
            implementationClass = "com.github.durex.gradle.catalog.DurexCatalogPlugin"
        }
        create("durexModule") {
            id = "durex.module"
            implementationClass = "com.github.durex.gradle.DurexModulePlugin"
        }
        create("durexFixtureCapability") {
            id = "com.acme.durex.fixture"
            implementationClass = "com.github.durex.gradle.internaltesting.FixtureCapabilityPlugin"
        }
    }
}
