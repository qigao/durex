# Spring Native reference

This is an isolated Spring Boot 4.1 / GraalVM Native reference build for Durex.
It intentionally does not participate in the legacy root Gradle 7.4.2 build so the existing Quarkus 2.9 modules remain untouched while the native baseline is validated.

## Requirements

- Gradle 8.14.3 or newer in the Spring Boot 4.1 supported range
- Java 21+ for JVM builds
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
