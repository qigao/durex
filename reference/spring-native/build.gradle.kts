plugins {
    id("io.github.qigao.simpledsl.java")
}

group = "com.github.durex.reference"
version = "0.1.0-SNAPSHOT"

simpledsl {
    springService()
    web()
    messaging()
    redis()
    aop()
    nativeImage()
}

dependencies {
    add("implementation", project(":messaging-spring-redis"))
}
