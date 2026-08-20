plugins {
    java
    alias(sLibs.plugins.springboot4)
    alias(sLibs.plugins.graalvm)
}

group = "com.github.durex.reference"
version = "0.1.0-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

apply(from = rootProject.file("../../gradle/library/spring-core.gradle"))
apply(from = rootProject.file("../../gradle/library/spring-web.gradle"))
apply(from = rootProject.file("../../gradle/library/spring-test.gradle"))
