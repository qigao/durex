# Spring Native reference

This is an isolated Spring Boot 4.1 / GraalVM Native reference build for Durex.

The reference build consumes SimpleDSL `0.2.0` from the Gradle Plugin Portal:

- `io.github.qigao.simpledsl.settings` follows the configured `repositoryRoot` to auto-discover the repository-root `dependencies.toml`, which includes `dependencies/*.toml` fragments;
- `io.github.qigao.simpledsl.build` is the only project-side SimpleDSL plugin;
- `simpledsl { springService(); web(); nativeImage() }` composes the Spring service and GraalVM Native capabilities;
- Gradle BOM/platform resolution still performs the actual Spring dependency alignment.

The reference files remain Kotlin DSL so the published SimpleDSL settings/build extensions are continuously checked for Kotlin DSL compatibility.

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
