plugins {
    id("io.github.qigao.simpledsl.build")
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
    implementation(project(":messaging-spring-redis"))
}
