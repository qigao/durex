plugins {
    `groovy-gradle-plugin`
}

repositories {
    gradlePluginPortal()
    mavenCentral()
}

dependencies {
    implementation(dbLibs.jooq.spring.codegen.gradle)
    implementation(dbLibs.jooq.spring.core)
    implementation(dbLibs.jooq.spring.meta)
}
