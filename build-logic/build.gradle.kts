plugins {
    `groovy-gradle-plugin`
}

repositories {
    gradlePluginPortal()
    mavenCentral()
}

dependencies {
    implementation(dbLibs.jooq.spring.codegen.gradle)
}
