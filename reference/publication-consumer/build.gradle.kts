plugins {
    java
}

group = "io.github.qigao.durex.reference"
version = "0.1.0-SNAPSHOT"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

val durexVersion = providers.gradleProperty("durexVersion").orElse("0.1.0-SNAPSHOT")

dependencies {
    implementation("io.github.qigao.durex:shared-spring-http:${durexVersion.get()}")
    implementation("io.github.qigao.durex:messaging-spring-redis:${durexVersion.get()}")
}
