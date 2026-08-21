# Durex Spring Boot Platform v3 Phase B Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make HTTP an explicit Spring Boot capability, extract a reusable Music HTTP service contract, and prove the same contract works as a Spring MVC server API and a declarative HTTP client proxy.

**Architecture:** `durex.spring-service` becomes a thin executable application convention. `durex.feature.web` owns Spring MVC + validation + web tests; `durex.feature.http-client` owns Spring Boot's blocking RestClient/HTTP Service Client support. A separate `music-api` Spring library exposes only DTO/envelope types plus Spring HTTP Service annotations, while the service controller implements that interface and client code uses the same interface through Spring's generated proxy.

**Tech Stack:** Gradle 9.1 Durex build platform, Java 25, Spring Boot 4.1, Spring Framework 7 HTTP Service Interfaces, Spring MVC, RestClient, JUnit 5, MockMvc, H2, GitHub Actions.

**Spec:** `docs/superpowers/specs/2026-08-21-durex-spring-boot-platform-v3-design.md`

## Global Constraints

- Spring Boot remains the only supported application runtime.
- Durex must not implement a custom HTTP transport, router, client proxy engine, or HTTP annotation layer.
- `durex.spring-service` means executable Spring Boot application only; HTTP, validation, observability, and other runtime features are explicit capabilities.
- Public HTTP capabilities are `durex.feature.web` / `durex.web()` and `durex.feature.http-client` / `durex.httpClient()`.
- Spring MVC is the default blocking server runtime.
- Blocking client support uses Spring Boot's `spring-boot-starter-restclient` and Spring HTTP Service Interfaces.
- Reusable contracts use `@HttpExchange` / `@GetExchange` / `@PostExchange` / `@PutExchange` / `@DeleteExchange` directly; no Durex wrapper annotations.
- The reusable API contract must not depend on the service implementation module.
- Existing response envelopes and paths under `/v1/music` remain compatible.
- Phase B does not implement messaging, JSON schema plugin work, or observability as a Durex capability.

---

## File Structure

```text
build-logic/
├── src/main/groovy/durex.spring-service.gradle
├── src/main/groovy/durex.feature.web.gradle
├── src/main/groovy/durex.feature.http-client.gradle
├── src/main/groovy/com/github/durex/gradle/capability/BuiltinCapabilities.groovy
├── src/main/groovy/com/github/durex/gradle/DurexExtension.groovy
└── tests/
    ├── web-feature-smoke/
    └── http-client-feature-smoke/

gradle/dependencies/spring.toml

core/api/music/
├── build.spring.gradle
└── src/main/java/com/github/durex/music/api/MusicApi.java

core/music/
├── build.spring.gradle
├── src/spring/java/.../MusicHttpController.java
└── src/springTest/java/.../
    ├── MusicHttpControllerTest.java
    └── MusicHttpContractIntegrationTest.java

migration/spring-music/modules.toml
.github/workflows/
├── durex-plugin-api.yml
└── spring-music.yml
```

---

### Task 1: Split Spring service from HTTP runtime capabilities

**Files:**
- Modify: `gradle/dependencies/spring.toml`
- Modify: `build-logic/src/main/groovy/durex.spring-service.gradle`
- Modify: `build-logic/src/main/groovy/com/github/durex/gradle/capability/BuiltinCapabilities.groovy`
- Modify: `build-logic/src/main/groovy/com/github/durex/gradle/DurexExtension.groovy`
- Create: `build-logic/src/main/groovy/durex.feature.web.gradle`
- Create: `build-logic/src/main/groovy/durex.feature.http-client.gradle`
- Create: `build-logic/tests/web-feature-smoke/settings.gradle`
- Create: `build-logic/tests/web-feature-smoke/build.gradle`
- Create: `build-logic/tests/http-client-feature-smoke/settings.gradle`
- Create: `build-logic/tests/http-client-feature-smoke/build.gradle`
- Modify: `build-logic/tests/verify-plugin-namespaces.sh`
- Modify: `.github/workflows/durex-plugin-api.yml`

**Interfaces:**
- Produces capability `web` through plugin `durex.feature.web` and DSL `durex.web()`.
- Produces capability `http-client` through plugin `durex.feature.http-client` and DSL `durex.httpClient()`.

- [ ] **Step 1: Add failing feature fixtures**

`web-feature-smoke/build.gradle`:

```gradle
plugins { id 'durex.spring-service' }

durex { web() }

import com.github.durex.gradle.model.DurexModuleModel
assert extensions.getByType(DurexModuleModel).capabilities.get().contains('web')
tasks.register('verifyWebCapability')
```

`http-client-feature-smoke/build.gradle`:

```gradle
plugins { id 'durex.spring-library' }

durex { httpClient() }

import com.github.durex.gradle.model.DurexModuleModel
assert extensions.getByType(DurexModuleModel).capabilities.get().contains('http-client')
tasks.register('verifyHttpClientCapability')
```

Use the same settings bootstrap as `transaction-feature-smoke`.

- [ ] **Step 2: Verify RED**

Run:

```bash
gradle -p build-logic/tests/web-feature-smoke verifyWebCapability --stacktrace
gradle -p build-logic/tests/http-client-feature-smoke verifyHttpClientCapability --stacktrace
```

Expected: both fail because the DSL methods/plugins do not exist.

- [ ] **Step 3: Add precise managed dependencies**

In `spring.toml`, replace the generic web aliases with:

```toml
[libraries.spring-http]
module = "org.springframework:spring-web"
platform = "spring"

[libraries.spring-webmvc]
module = "org.springframework.boot:spring-boot-starter-webmvc"
platform = "spring"

[libraries.spring-webmvc-test]
module = "org.springframework.boot:spring-boot-starter-webmvc-test"
platform = "spring"

[libraries.spring-restclient]
module = "org.springframework.boot:spring-boot-starter-restclient"
platform = "spring"

[libraries.spring-restclient-test]
module = "org.springframework.boot:spring-boot-starter-restclient-test"
platform = "spring"
```

Keep `spring-validation`; it is composed by the web capability.

- [ ] **Step 4: Slim `durex.spring-service`**

Target dependency behavior:

```groovy
DurexDependencyAccess.add(project, model, 'implementation', 'spring-core')
DurexDependencyAccess.add(project, model, 'testImplementation', 'spring-test')
```

Remove implicit web, validation, observability, and web-test dependencies.

- [ ] **Step 5: Register WEB and HTTP_CLIENT capabilities**

Add:

```groovy
static final CapabilitySpec WEB = CapabilitySpec.builder('web')
        .allow(ModuleKind.SPRING_SERVICE)
        .dependency('implementation', 'spring-webmvc')
        .dependency('implementation', 'spring-validation')
        .dependency('testImplementation', 'spring-webmvc-test')
        .build()

static final CapabilitySpec HTTP_CLIENT = CapabilitySpec.builder('http-client')
        .allow(ModuleKind.SPRING_LIBRARY, ModuleKind.SPRING_SERVICE)
        .dependency('implementation', 'spring-restclient')
        .dependency('testImplementation', 'spring-restclient-test')
        .build()
```

Register both in `registerAll`.

- [ ] **Step 6: Add public plugins and DSL methods**

Create precompiled feature plugins following `durex.feature.transaction.gradle`.

Add:

```groovy
void web() {
    project.pluginManager.apply('durex.feature.web')
}

void httpClient() {
    project.pluginManager.apply('durex.feature.http-client')
}
```

- [ ] **Step 7: Extend namespace and CI contracts**

Require both new feature plugin files in `verify-plugin-namespaces.sh` and add smoke commands to `durex-plugin-api.yml`.

- [ ] **Step 8: Verify GREEN and service slimming**

Run:

```bash
gradle -p build-logic/tests/web-feature-smoke verifyWebCapability dependencies --configuration runtimeClasspath --stacktrace
gradle -p build-logic/tests/http-client-feature-smoke verifyHttpClientCapability dependencies --configuration runtimeClasspath --stacktrace
gradle -p build-logic/tests/spring-service-smoke dependencies --configuration runtimeClasspath --stacktrace
```

Expected:
- web fixture contains WebMVC/validation;
- client fixture contains RestClient HTTP service support;
- plain spring-service runtimeClasspath does not contain WebMVC/Actuator solely from the module type.

- [ ] **Step 9: Commit**

```bash
git commit -m "feat: add explicit Spring HTTP capabilities"
```

---

### Task 2: Extract a reusable Music HTTP Service Interface

**Files:**
- Create: `core/api/music/build.spring.gradle`
- Create: `core/api/music/src/main/java/com/github/durex/music/api/MusicApi.java`
- Modify: `migration/spring-music/modules.toml`
- Modify: `core/music/build.spring.gradle`
- Modify: `core/music/src/spring/java/com/github/durex/music/spring/MusicHttpController.java`
- Modify: `core/music/src/springTest/java/com/github/durex/music/spring/MusicHttpControllerTest.java`

**Interfaces:**
- Produces `com.github.durex.music.api.MusicApi`.
- Server implementation remains `MusicHttpController` but now implements `MusicApi`.

- [ ] **Step 1: Add the API module and contract first**

`build.spring.gradle`:

```gradle
plugins { id 'durex.spring-library' }

durex {
    dependency('api', 'spring-http')
}

dependencies {
    api project(':shared-common')
    api project(':music-json')
}
```

`MusicApi.java`:

```java
package com.github.durex.music.api;

import com.github.durex.music.model.Music;
import com.github.durex.shared.model.RespData;
import java.util.List;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.DeleteExchange;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;
import org.springframework.web.service.annotation.PutExchange;

@HttpExchange("/v1/music")
public interface MusicApi {
  @GetExchange({"", "/"})
  RespData<List<Music>> list(
      @RequestParam(value = "title", required = false) String title,
      @RequestParam(value = "id", required = false) String musicId,
      @RequestParam(value = "offset", defaultValue = "10") int offset);

  @GetExchange("/{id}")
  RespData<Music> get(@PathVariable("id") String musicId);

  @PostExchange({"", "/"})
  RespData<Integer> create(@RequestBody Music music);

  @PutExchange({"", "/"})
  RespData<Integer> update(@RequestBody Music music);

  @DeleteExchange("/{id}")
  RespData<Integer> delete(@PathVariable("id") String musicId);
}
```

- [ ] **Step 2: Register `music-api` in the Spring migration graph**

Add a manual module entry for `core/api/music` and make `:music` depend on `project(':music-api')`.

Activate:

```gradle
durex {
    web()
    aop()
    transaction()
    ...
}
```

- [ ] **Step 3: Convert the controller to implement the contract**

Replace method mapping annotations on the controller with the interface contract. Keep only:

```java
@RestController
public class MusicHttpController implements MusicApi {
```

Rename methods to `list`, `get`, `create`, `update`, `delete` and annotate implementations with `@Override` only.

- [ ] **Step 4: Update standalone controller tests**

Keep the same paths and response assertions, but call the refactored controller through MockMvc. Expected existing HTTP behavior remains unchanged.

- [ ] **Step 5: Verify server contract GREEN**

Run:

```bash
gradle -p migration/spring-music :music-api:compileJava :music:test --tests '*MusicHttpControllerTest' --stacktrace
```

Expected: contract compiles independently and all legacy-compatible server endpoint tests pass.

- [ ] **Step 6: Commit**

```bash
git commit -m "refactor: extract Music HTTP service contract"
```

---

### Task 3: Prove the same contract through a real declarative HTTP client proxy

**Files:**
- Modify: `core/music/build.spring.gradle`
- Create: `core/music/src/springTest/java/com/github/durex/music/spring/MusicHttpContractIntegrationTest.java`

**Interfaces:**
- Consumes `MusicApi` from Task 2.
- Produces proof that Spring's `HttpServiceProxyFactory` can call the running Music server through the same interface.

- [ ] **Step 1: Activate client support for integration tests**

In the Music module enable:

```gradle
durex {
    web()
    httpClient()
    ...
}
```

- [ ] **Step 2: Write the end-to-end contract test**

Use `@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)`, inject `@LocalServerPort int port`, and build the client in the test:

```java
RestClient restClient = RestClient.builder()
    .baseUrl("http://localhost:" + port)
    .build();
HttpServiceProxyFactory factory = HttpServiceProxyFactory
    .builderFor(RestClientAdapter.create(restClient))
    .build();
MusicApi client = factory.createClient(MusicApi.class);
```

Assert:

```java
var response = client.get("music-1");
assertEquals("music-1", response.getResult().getId());
assertEquals("Spring Runtime Song", response.getResult().getTitle());
```

This test must hit the real random-port server backed by H2, not MockMvc.

- [ ] **Step 3: Verify RED then GREEN**

Before client capability wiring, expect missing RestClient/adapter classes. After wiring, run:

```bash
gradle -p migration/spring-music :music:test --tests '*MusicHttpContractIntegrationTest' --stacktrace
```

Expected: PASS through actual HTTP serialization, routing, service, jOOQ, H2, response envelope, deserialization, and HTTP service proxy.

- [ ] **Step 4: Commit**

```bash
git commit -m "test: prove Music HTTP contract client and server"
```

---

### Task 4: Add Spring Boot HTTP Service group auto-configuration coverage

**Files:**
- Create: `core/music/src/springTest/java/com/github/durex/music/spring/MusicHttpServiceGroupTest.java`

**Interfaces:**
- Validates Spring Boot's native HTTP service client registration; Durex adds no competing configuration model.

- [ ] **Step 1: Define a client-only test configuration**

Use:

```java
@TestConfiguration(proxyBeanMethods = false)
@ImportHttpServices(group = "music", types = MusicApi.class)
static class HttpClientConfiguration {}
```

Start a JDK `HttpServer` on an ephemeral port in `@BeforeAll` that returns a valid `RespData<Music>` JSON payload for `/v1/music/music-1`.

Register:

```java
@DynamicPropertySource
static void httpClientProperties(DynamicPropertyRegistry registry) {
  registry.add("spring.http.serviceclient.music.base-url", () -> baseUrl);
}
```

- [ ] **Step 2: Autowire the generated `MusicApi` bean and call it**

Assert the proxy returns the expected Music object. This proves Boot's native service-group property model and bean registration are sufficient; no Durex client auto-configuration class is needed.

- [ ] **Step 3: Verify GREEN**

Run:

```bash
gradle -p migration/spring-music :music:test --tests '*MusicHttpServiceGroupTest' --stacktrace
```

Expected: generated `MusicApi` bean uses `spring.http.serviceclient.music.base-url` and succeeds.

- [ ] **Step 4: Commit**

```bash
git commit -m "test: validate Spring HTTP service group configuration"
```

---

### Task 5: Add HTTP platform regression gates

**Files:**
- Modify: `.github/workflows/spring-music.yml`
- Modify: `.github/workflows/durex-build-platform.yml`
- Modify: `docs/superpowers/specs/2026-08-21-durex-spring-boot-platform-v3-design.md`

- [ ] **Step 1: Extend Spring Music validation**

Ensure the normal `:music:test` suite includes both HTTP contract tests and add an explicit `:music-api:compileJava` to the compile graph.

- [ ] **Step 2: Extend platform regression**

Run both new capability fixtures and compile `:music-api` as part of the platform workflow.

- [ ] **Step 3: Add a negative layering guard**

Fail if `core/api/music/build.spring.gradle` depends on `project(':music')`, ensuring clients/contracts never depend on server implementation.

- [ ] **Step 4: Mark Phase B exit status in the architecture spec**

Document that Phase B is complete only when:

- `durex.spring-service` no longer implicitly supplies WebMVC/Actuator;
- web and blocking HTTP client are explicit capabilities;
- `MusicApi` is a reusable contract module;
- the controller implements it;
- the same contract succeeds through a real RestClient HTTP service proxy;
- Boot HTTP service group configuration is proven without Durex transport/config duplication.

- [ ] **Step 5: Verify the complete phase**

Required PR workflows:

```text
Durex Plugin API
Durex Build Platform
Spring Music Migration
Spring Native Reference
```

- [ ] **Step 6: Commit**

```bash
git commit -m "ci: enforce Spring HTTP platform contract"
```

---

## Self-Review Notes

- **Spec coverage:** Phase B covers explicit web/client capabilities, reusable HTTP Service Interface, server implementation, real generated client proxy, native Boot service-group configuration, and HTTP regression gates.
- **No custom transport:** Durex only controls Gradle capability composition; Spring owns routing, RestClient, proxy creation, properties, conversion, validation, and observation hooks.
- **Layering:** `music-api` depends on DTO/envelope contracts and `spring-web` annotations only. It never depends on `music` service implementation.
- **Module type cleanup:** `durex.spring-service` is intentionally slimmed here because HTTP is the first consumer that proves feature orthogonality.
- **Deferred:** custom authentication policies, WebClient/reactive clients, Durex observability capability, messaging, and schema v2 remain outside Phase B.
