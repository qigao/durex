plugins {
    java
}

apply(from = rootProject.file("../../gradle/library/spring-core.gradle"))
apply(from = rootProject.file("../../gradle/library/spring-web.gradle"))
apply(from = rootProject.file("../../gradle/library/spring-data-jpa.gradle"))
apply(from = rootProject.file("../../gradle/library/spring-jdbc.gradle"))
apply(from = rootProject.file("../../gradle/library/spring-jooq.gradle"))
apply(from = rootProject.file("../../gradle/library/spring-redis.gradle"))
apply(from = rootProject.file("../../gradle/library/spring-observability.gradle"))
