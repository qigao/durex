# Spring Native reference

This is an isolated Spring Boot 4.1 / GraalVM Native reference build for Durex.
It intentionally does not participate in the legacy root Gradle 7.4.2 build so the existing Quarkus 2.9 modules remain untouched while the Spring-first platform is migrated.

The reference build consumes Durex's central Gradle management instead of declaring Spring/GraalVM versions and starter sets locally:

- `gradle/versions/spring.versions.toml` owns Spring Boot and GraalVM plugin versions;
- `gradle/library/spring-core.gradle` provides the Spring core capability;
- `gradle/library/spring-web.gradle` provides Spring MVC;
- `gradle/library/spring-test.gradle` provides Spring/JUnit testing.

## Requirements

- Gradle 9.1+ when Gradle itself runs on Java 25
- Java 21+ source compatibility
- GraalVM 25 with `native-image` for native builds

## JVM test

```bash
gradle test
```

## AOT processing

```bash
gradle processAot processTestAot
```

## Native test

```bash
gradle nativeTest
```

## Native executable

```bash
gradle nativeCompile
./build/native/nativeCompile/spring-native-reference
```

Then open `http://localhost:8080/hello`.

## Native container image

```bash
gradle bootBuildImage
```
