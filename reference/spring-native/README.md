# Spring Native reference

This is an isolated Spring Boot 4.1 / GraalVM Native reference build for Durex.
It intentionally does not participate in the legacy root Gradle 7.4.2 build while the Spring-first platform is migrated.

The reference build now consumes the Durex build platform directly:

- `gradle/dependencies/*.toml` is the Durex-owned dependency/plugin version source;
- `durex.spring-service` provides the Spring service baseline;
- `durex { nativeImage() }` enables GraalVM Native as an optional feature;
- Gradle BOM/platform resolution still performs the actual Spring dependency alignment.

The reference files remain Kotlin DSL so Durex settings/module extensions are continuously checked for Kotlin DSL compatibility.

## Requirements

- Gradle 9.1+
- Java/GraalVM 25
- `native-image` for native builds

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
