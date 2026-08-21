# Durex Spring Boot Platform v3 Phase A Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Remove CDI/`javax.inject`/`javax.transaction` from the active Spring Music graph, add a first-class transaction capability, and establish the reusable Spring Boot runtime wiring pattern that later HTTP and messaging phases can build on.

**Architecture:** Keep business/repository logic in neutral Java classes and move framework lifecycle/injection into thin runtime adapters. Spring uses `@Bean`, Spring AOP, and Spring `@Transactional`; legacy Quarkus compatibility, where still needed before final deletion, is isolated in `src/quarkus/java` wrappers rather than leaking CDI annotations back into neutral code. Durex build logic exposes transaction as an orthogonal capability, while Spring Boot remains responsible for transaction infrastructure and auto-configuration.

**Tech Stack:** Gradle 9.1 Durex build platform, Java 25 Spring migration graph, Spring Boot 4.1, Spring Framework transaction/AOP, jOOQ, H2, JUnit 5, GitHub Actions; legacy Quarkus root build remains transitional only until final removal.

**Spec:** `docs/superpowers/specs/2026-08-21-durex-spring-boot-platform-v3-design.md`

## Global Constraints

- Spring Boot is the only target application runtime; Phase A must not introduce new Quarkus runtime abstractions.
- Durex does not implement its own DI container or transaction manager.
- `jakarta.persistence.*` and `jakarta.validation.*` remain valid standards; `javax.enterprise.*`, `javax.inject.*`, `javax.interceptor.*`, and `javax.transaction.*` must disappear from the active Spring graph.
- `durex.spring-service` remains an executable-module type, not a catch-all runtime dependency bundle.
- Transaction support is an orthogonal public capability: `durex.feature.transaction` and DSL shortcut `durex.transaction()`.
- Spring transaction semantics use `org.springframework.transaction.annotation.Transactional`; no Durex transaction annotation is introduced.
- Reusable Spring runtime integration uses Spring Boot auto-configuration and `@ConditionalOnMissingBean` where defaults are overridable.
- Existing #132 AOP semantics (`@NullChecker`, `@ValueChecker`) must remain green.
- HTTP and messaging feature implementation are out of scope for Phase A.
- Root Gradle modernization and full Quarkus deletion remain separate cleanup after the active Spring graph no longer depends on the legacy runtime.

---

## File Structure

Phase A changes are intentionally grouped by responsibility:

```text
build-logic/
├── src/main/groovy/durex.feature.transaction.gradle
├── src/main/groovy/com/github/durex/gradle/capability/BuiltinCapabilities.groovy
├── src/main/groovy/com/github/durex/gradle/DurexExtension.groovy
└── tests/transaction-feature-smoke/

gradle/dependencies/spring.toml

core/schema/music/repo/
├── src/main/java/.../repository/*.java           # neutral implementations
└── src/quarkus/java/.../repository/quarkus/*.java # temporary CDI wrappers

core/music/
├── src/main/java/.../service/*.java               # neutral service logic
├── src/spring/java/.../spring/*                   # Spring wiring + tx adapter
└── src/quarkus/java/.../service/quarkus/*.java    # temporary CDI wrappers

migration/spring-music/
└── modules.toml

.github/workflows/
├── durex-build-platform.yml
└── spring-music.yml
```

The `src/quarkus/java` wrappers are temporary migration scaffolding only. They contain no business logic and are deleted with the legacy runtime; they exist solely so neutralizing `src/main/java` does not force business-code duplication.

---

### Task 1: Add the transaction capability to the Durex build platform

**Files:**
- Modify: `gradle/dependencies/spring.toml`
- Modify: `build-logic/src/main/groovy/com/github/durex/gradle/capability/BuiltinCapabilities.groovy`
- Modify: `build-logic/src/main/groovy/com/github/durex/gradle/DurexExtension.groovy`
- Create: `build-logic/src/main/groovy/durex.feature.transaction.gradle`
- Create: `build-logic/tests/transaction-feature-smoke/settings.gradle`
- Create: `build-logic/tests/transaction-feature-smoke/build.gradle`
- Modify: `build-logic/tests/verify-plugin-namespaces.sh`
- Modify: `.github/workflows/durex-plugin-api.yml`

**Interfaces:**
- Consumes: the existing capability engine and Spring dependency platform.
- Produces: public plugin ID `durex.feature.transaction`, capability id `transaction`, and DSL method `durex.transaction()`.

- [ ] **Step 1: Write the failing capability fixture**

Create `build-logic/tests/transaction-feature-smoke/build.gradle`:

```gradle
plugins {
    id 'durex.spring-service'
}

durex {
    transaction()
}

import com.github.durex.gradle.model.DurexModuleModel

assert extensions.getByType(DurexModuleModel).capabilities.get().contains('transaction')

tasks.register('verifyTransactionCapability')
```

Use the same settings bootstrap shape as `build-logic/tests/aop-feature-smoke/settings.gradle`.

- [ ] **Step 2: Run the fixture and verify RED**

Run:

```bash
gradle -p build-logic/tests/transaction-feature-smoke verifyTransactionCapability --stacktrace
```

Expected: FAIL because `DurexExtension.transaction()` / `durex.feature.transaction` does not exist.

- [ ] **Step 3: Add the managed Spring transaction dependency**

Add to `gradle/dependencies/spring.toml`:

```toml
[libraries.spring-transaction]
module = "org.springframework:spring-tx"
platform = "spring"
```

Do not add an explicit version; Spring Boot owns it through the `spring` platform.

- [ ] **Step 4: Register the built-in capability**

Add to `BuiltinCapabilities.groovy`:

```groovy
static final CapabilitySpec TRANSACTION = CapabilitySpec.builder('transaction')
        .allow(ModuleKind.SPRING_LIBRARY, ModuleKind.SPRING_SERVICE)
        .dependency('implementation', 'spring-transaction')
        .build()
```

Register it with the built-ins:

```groovy
[AOP, TRANSACTION, JPA, JDBC, JOOQ, REDIS, NATIVE, LOMBOK].each { registry.register(it) }
```

- [ ] **Step 5: Add the public feature plugin and DSL shortcut**

Create `durex.feature.transaction.gradle`:

```gradle
import com.github.durex.gradle.capability.BuiltinCapabilities
import com.github.durex.gradle.capability.DurexCapabilitySupport

plugins { id 'durex.module' }

DurexCapabilitySupport.registerAndEnable(
    project,
    'durex.feature.transaction',
    BuiltinCapabilities.TRANSACTION
)
```

Add to `DurexExtension`:

```groovy
void transaction() {
    project.pluginManager.apply('durex.feature.transaction')
}
```

- [ ] **Step 6: Extend the public-plugin contract**

In `verify-plugin-namespaces.sh`, require:

```bash
require_file build-logic/src/main/groovy/durex.feature.transaction.gradle
```

Add a CI step to `.github/workflows/durex-plugin-api.yml`:

```yaml
- name: Verify transaction feature capability
  run: gradle -p build-logic/tests/transaction-feature-smoke verifyTransactionCapability dependencies --configuration compileClasspath --stacktrace
```

- [ ] **Step 7: Verify GREEN**

Run:

```bash
gradle -p build-logic/tests/transaction-feature-smoke verifyTransactionCapability dependencies --configuration compileClasspath --stacktrace
bash build-logic/tests/verify-plugin-namespaces.sh
```

Expected:
- capability set contains `transaction`;
- compile classpath contains Spring transaction classes through `org.springframework:spring-tx`;
- namespace contract succeeds.

- [ ] **Step 8: Commit**

```bash
git add -- \
  gradle/dependencies/spring.toml \
  build-logic/src/main/groovy/com/github/durex/gradle/capability/BuiltinCapabilities.groovy \
  build-logic/src/main/groovy/com/github/durex/gradle/DurexExtension.groovy \
  build-logic/src/main/groovy/durex.feature.transaction.gradle \
  build-logic/tests/transaction-feature-smoke \
  build-logic/tests/verify-plugin-namespaces.sh \
  .github/workflows/durex-plugin-api.yml
git commit -m "feat: add Spring transaction capability"
```

---

### Task 2: Neutralize jOOQ repository implementations

**Files:**
- Modify: `core/schema/music/repo/src/main/java/com/github/durex/music/repository/MusicRepository.java`
- Modify: `core/schema/music/repo/src/main/java/com/github/durex/music/repository/PlayListRepository.java`
- Modify: `core/schema/music/repo/src/main/java/com/github/durex/music/repository/PlayListMusicRepository.java`
- Modify: `core/schema/music/repo/src/main/java/com/github/durex/music/repository/CreatorPlayListRepository.java`
- Create: `core/schema/music/repo/src/quarkus/java/com/github/durex/music/repository/quarkus/QuarkusMusicRepository.java`
- Create: `core/schema/music/repo/src/quarkus/java/com/github/durex/music/repository/quarkus/QuarkusPlayListRepository.java`
- Create: `core/schema/music/repo/src/quarkus/java/com/github/durex/music/repository/quarkus/QuarkusPlayListMusicRepository.java`
- Create: `core/schema/music/repo/src/quarkus/java/com/github/durex/music/repository/quarkus/QuarkusCreatorPlayListRepository.java`
- Modify: `core/schema/music/repo/build.gradle`
- Modify: `core/schema/music/repo/build.spring.gradle`
- Create: `core/music/src/springTest/java/com/github/durex/music/spring/RepositoryWiringTest.java`

**Interfaces:**
- Consumes: `org.jooq.DSLContext`, generated jOOQ tables/records, Jakarta Validation.
- Produces: four plain Java repository classes with explicit `DSLContext` constructors; temporary Quarkus wrappers provide CDI scopes without contaminating the neutral classes.

- [ ] **Step 1: Add a failing Spring wiring test for all repositories**

Create `RepositoryWiringTest.java`:

```java
package com.github.durex.music.spring;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.github.durex.music.repository.CreatorPlayListRepository;
import com.github.durex.music.repository.MusicRepository;
import com.github.durex.music.repository.PlayListMusicRepository;
import com.github.durex.music.repository.PlayListRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = MusicSpringApplication.class)
class RepositoryWiringTest {
  @Autowired MusicRepository musicRepository;
  @Autowired PlayListRepository playListRepository;
  @Autowired PlayListMusicRepository playListMusicRepository;
  @Autowired CreatorPlayListRepository creatorPlayListRepository;

  @Test
  void repositoriesAreConstructedBySpringWithoutCdi() {
    assertNotNull(musicRepository);
    assertNotNull(playListRepository);
    assertNotNull(playListMusicRepository);
    assertNotNull(creatorPlayListRepository);
  }
}
```

- [ ] **Step 2: Run the Spring test and verify RED**

Run:

```bash
gradle -p migration/spring-music :music:test --tests '*RepositoryWiringTest' --stacktrace
```

Expected: FAIL because only `MusicRepository` is currently declared as a Spring bean.

- [ ] **Step 3: Convert repository classes to constructor-only Java objects**

For each repository:
- remove `javax.enterprise.context.RequestScoped`;
- remove `javax.inject.Inject`;
- remove `@RequestScoped` and field injection;
- declare `private final DSLContext dsl`;
- add a public constructor.

Pattern:

```java
public class PlayListRepository {
  private final DSLContext dsl;

  public PlayListRepository(DSLContext dsl) {
    this.dsl = dsl;
  }

  // existing query/update methods unchanged
}
```

For `MusicRepository`, retain its existing constructor but remove CDI annotations/imports.

- [ ] **Step 4: Migrate validation annotations to Jakarta**

Replace:

```java
import javax.validation.constraints.NotNull;
```

with:

```java
import jakarta.validation.constraints.NotNull;
```

Do not remove validation semantics just to eliminate `javax.*`.

- [ ] **Step 5: Make the Spring repository module depend only on Jakarta validation**

In `core/schema/music/repo/build.spring.gradle`, remove:

```gradle
compileOnly durex.library('javax-cdi')
compileOnly durex.library('javax-inject')
compileOnly durex.library('javax-validation')
```

Add:

```gradle
dependencies {
    implementation project(':shared-utils')
    api project(':music-json')
    implementation project(':music-entity')
    api durex.library('jakarta-validation')
}
```

- [ ] **Step 6: Add temporary Quarkus CDI wrappers without business logic**

Example `QuarkusMusicRepository.java`:

```java
package com.github.durex.music.repository.quarkus;

import com.github.durex.music.repository.MusicRepository;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import org.jooq.DSLContext;

@RequestScoped
public class QuarkusMusicRepository extends MusicRepository {
  @Inject
  public QuarkusMusicRepository(DSLContext dsl) {
    super(dsl);
  }
}
```

Create equivalent wrappers for `PlayListRepository`, `PlayListMusicRepository`, and `CreatorPlayListRepository`.

- [ ] **Step 7: Add the temporary wrapper source root only to the legacy build**

In `core/schema/music/repo/build.gradle` add:

```gradle
sourceSets {
  main {
    java.srcDir 'src/quarkus/java'
  }
}
```

Do not add this source root to `build.spring.gradle`.

- [ ] **Step 8: Wire all neutral repositories in Spring configuration**

Extend `MusicSpringConfiguration` with:

```java
@Bean
PlayListRepository playListRepository(DSLContext dsl) {
  return new PlayListRepository(dsl);
}

@Bean
PlayListMusicRepository playListMusicRepository(DSLContext dsl) {
  return new PlayListMusicRepository(dsl);
}

@Bean
CreatorPlayListRepository creatorPlayListRepository(DSLContext dsl) {
  return new CreatorPlayListRepository(dsl);
}
```

- [ ] **Step 9: Verify Spring and legacy compile paths**

Run:

```bash
gradle -p migration/spring-music :music-repo:compileJava :music:test --tests '*RepositoryWiringTest' --stacktrace
./gradlew :core:schema:music:repo:compileJava --stacktrace
```

Expected: both compile; Spring test passes.

- [ ] **Step 10: Commit**

```bash
git add -- \
  core/schema/music/repo/src/main/java/com/github/durex/music/repository \
  core/schema/music/repo/src/quarkus/java \
  core/schema/music/repo/build.gradle \
  core/schema/music/repo/build.spring.gradle \
  core/music/src/spring/java/com/github/durex/music/spring/MusicSpringConfiguration.java \
  core/music/src/springTest/java/com/github/durex/music/spring/RepositoryWiringTest.java
git commit -m "refactor: make music repositories runtime neutral"
```

---

### Task 3: Neutralize service construction and move transaction semantics to Spring

**Files:**
- Modify: `core/music/src/main/java/com/github/durex/music/service/MusicService.java`
- Modify: `core/music/src/main/java/com/github/durex/music/service/PlaylistService.java`
- Create: `core/music/src/spring/java/com/github/durex/music/spring/SpringPlaylistService.java`
- Create: `core/music/src/quarkus/java/com/github/durex/music/service/quarkus/QuarkusMusicService.java`
- Create: `core/music/src/quarkus/java/com/github/durex/music/service/quarkus/QuarkusPlaylistService.java`
- Modify: `core/music/src/spring/java/com/github/durex/music/spring/MusicSpringConfiguration.java`
- Modify: `core/music/build.gradle`
- Modify: `core/music/build.spring.gradle`
- Create: `core/music/src/springTest/java/com/github/durex/music/spring/ServiceWiringTest.java`

**Interfaces:**
- Consumes: neutral repositories from Task 2 and `durex.feature.transaction` from Task 1.
- Produces: plain `MusicService`/`PlaylistService` business classes, Spring transaction adapter `SpringPlaylistService`, and temporary CDI subclasses for legacy compilation.

- [ ] **Step 1: Write the failing service wiring/transaction test**

Create `ServiceWiringTest.java`:

```java
package com.github.durex.music.spring;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.durex.music.service.MusicService;
import com.github.durex.music.service.PlaylistService;
import org.junit.jupiter.api.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = MusicSpringApplication.class)
class ServiceWiringTest {
  @Autowired MusicService musicService;
  @Autowired PlaylistService playlistService;

  @Test
  void servicesUseSpringWiringAndPlaylistServiceIsTransactionalProxy() {
    assertNotNull(musicService);
    assertNotNull(playlistService);
    assertTrue(AopUtils.isAopProxy(playlistService));
  }
}
```

- [ ] **Step 2: Run the test and verify RED**

Run:

```bash
gradle -p migration/spring-music :music:test --tests '*ServiceWiringTest' --stacktrace
```

Expected: FAIL because `PlaylistService` is not a Spring bean and/or not transaction-proxied.

- [ ] **Step 3: Remove CDI from `MusicService`**

Keep the existing constructor and remove:

```java
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
@Inject
```

Target shape:

```java
public class MusicService {
  private final MusicRepository repository;

  public MusicService(MusicRepository repository) {
    this.repository = repository;
  }

  // existing methods unchanged
}
```

- [ ] **Step 4: Remove CDI and transaction annotations from neutral `PlaylistService`**

Replace field injection with explicit constructor injection:

```java
public class PlaylistService {
  protected final PlayListRepository repository;
  protected final PlayListMusicRepository playListMusicRepository;

  public PlaylistService(
      PlayListRepository repository,
      PlayListMusicRepository playListMusicRepository) {
    this.repository = repository;
    this.playListMusicRepository = playListMusicRepository;
  }

  // existing business methods unchanged, no javax.transaction annotation
}
```

Remove all `javax.enterprise`, `javax.inject`, and `javax.transaction` imports/annotations.

- [ ] **Step 5: Add the Spring transaction adapter**

Create `SpringPlaylistService.java`:

```java
package com.github.durex.music.spring;

import com.github.durex.music.repository.PlayListMusicRepository;
import com.github.durex.music.repository.PlayListRepository;
import com.github.durex.music.service.PlaylistService;
import org.springframework.transaction.annotation.Transactional;

@Transactional
final class SpringPlaylistService extends PlaylistService {
  SpringPlaylistService(
      PlayListRepository repository,
      PlayListMusicRepository playListMusicRepository) {
    super(repository, playListMusicRepository);
  }
}
```

The adapter contains no business logic. Phase A intentionally applies one transaction boundary to the whole playlist service rather than duplicating every legacy method annotation. If later profiling proves read-only methods need narrower attributes, refine them with Spring annotations in a later change.

- [ ] **Step 6: Add temporary Quarkus service wrappers**

`QuarkusMusicService.java`:

```java
package com.github.durex.music.service.quarkus;

import com.github.durex.music.repository.MusicRepository;
import com.github.durex.music.service.MusicService;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class QuarkusMusicService extends MusicService {
  @Inject
  public QuarkusMusicService(MusicRepository repository) {
    super(repository);
  }
}
```

`QuarkusPlaylistService.java`:

```java
package com.github.durex.music.service.quarkus;

import com.github.durex.music.repository.PlayListMusicRepository;
import com.github.durex.music.repository.PlayListRepository;
import com.github.durex.music.service.PlaylistService;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;

@ApplicationScoped
@Transactional
public class QuarkusPlaylistService extends PlaylistService {
  @Inject
  public QuarkusPlaylistService(
      PlayListRepository repository,
      PlayListMusicRepository playListMusicRepository) {
    super(repository, playListMusicRepository);
  }
}
```

These wrappers are intentionally disposable; no new Quarkus behavior may be added to them.

- [ ] **Step 7: Add the Quarkus wrapper source root only to the legacy music build**

In `core/music/build.gradle`:

```gradle
sourceSets {
  main {
    java.srcDir 'src/quarkus/java'
  }
}
```

- [ ] **Step 8: Activate transaction capability in the Spring build**

In `core/music/build.spring.gradle` add:

```gradle
durex {
    persistence {
        jooq()
    }
    aop()
    transaction()
    lombok()
    dependency('testRuntimeOnly', 'h2')
}
```

Do not rely on incidental `spring-tx` transitives from jOOQ/JDBC starters; the capability declares the semantic dependency explicitly.

- [ ] **Step 9: Remove Spring compile-only CDI/transaction dependencies**

Delete from `core/music/build.spring.gradle`:

```gradle
compileOnly durex.library('javax-cdi')
compileOnly durex.library('javax-inject')
compileOnly durex.library('javax-transaction')
```

- [ ] **Step 10: Wire services through Spring configuration**

Extend `MusicSpringConfiguration`:

```java
@Bean
MusicService musicService(MusicRepository repository) {
  return new MusicService(repository);
}

@Bean
PlaylistService playlistService(
    PlayListRepository repository,
    PlayListMusicRepository playListMusicRepository) {
  return new SpringPlaylistService(repository, playListMusicRepository);
}
```

- [ ] **Step 11: Verify Spring AOP + transaction proxy + legacy compilation**

Run:

```bash
gradle -p migration/spring-music :music:durexDoctor :music:compileJava :music:test --configuration-cache --stacktrace
./gradlew :core:music:compileJava --stacktrace
```

Expected:
- `MusicSpringInterceptorTest` remains green;
- `ServiceWiringTest` sees `PlaylistService` as an AOP proxy;
- Spring compilation needs no CDI/inject/transaction compile-only aliases;
- legacy wrapper compilation remains green during the transition.

- [ ] **Step 12: Commit**

```bash
git add -- \
  core/music/src/main/java/com/github/durex/music/service \
  core/music/src/spring/java/com/github/durex/music/spring \
  core/music/src/quarkus/java \
  core/music/src/springTest/java/com/github/durex/music/spring/ServiceWiringTest.java \
  core/music/build.gradle \
  core/music/build.spring.gradle
git commit -m "refactor: move music runtime wiring to Spring"
```

---

### Task 4: Add a hard no-CDI contract for the active Spring graph

**Files:**
- Create: `migration/spring-music/verify-runtime-boundary.sh`
- Modify: `.github/workflows/spring-music.yml`
- Modify: `.github/workflows/durex-build-platform.yml`

**Interfaces:**
- Consumes: the Spring migration Gradle graph after Tasks 1–3.
- Produces: a CI guard that fails if legacy Java EE/CDI runtime dependencies or imports leak back into active Spring sources/build descriptors.

- [ ] **Step 1: Write the boundary script so it fails on the current pre-refactor tree**

Create `migration/spring-music/verify-runtime-boundary.sh`:

```bash
#!/usr/bin/env bash
set -euo pipefail

repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
cd "$repo_root"

fail() {
  echo "Spring runtime boundary violation: $*" >&2
  exit 1
}

for file in \
  core/music/build.spring.gradle \
  core/schema/music/repo/build.spring.gradle; do
  if grep -nE 'javax-(cdi|inject|interceptor|transaction|validation)' "$file"; then
    fail "$file contains a legacy javax dependency alias"
  fi
done

for path in \
  core/shared/common/src/main/java \
  core/shared/spring \
  core/music/src/main/java/com/github/durex/music/service \
  core/music/src/spring/java \
  core/schema/music/repo/src/main/java; do
  if grep -R -nE 'import javax\.(enterprise|inject|interceptor|transaction|validation)' "$path"; then
    fail "$path contains a legacy runtime import"
  fi
done

echo 'Spring runtime boundary: OK'
```

- [ ] **Step 2: Verify RED before Tasks 2–3 are complete**

Run:

```bash
bash migration/spring-music/verify-runtime-boundary.sh
```

Expected before neutralization: FAIL on current CDI/transaction imports or dependency aliases.

- [ ] **Step 3: Add dependency-graph verification**

In `.github/workflows/spring-music.yml`, after the existing compile/test step, add:

```yaml
- name: Verify Spring runtime boundary
  shell: bash
  run: |
    bash migration/spring-music/verify-runtime-boundary.sh
    gradle -p migration/spring-music :music:dependencies --configuration compileClasspath --stacktrace | tee /tmp/music-compile-classpath.log
    if grep -E 'javax\.enterprise|javax\.inject:|javax\.transaction|javax\.interceptor' /tmp/music-compile-classpath.log; then
      echo 'legacy javax runtime dependency leaked into Spring compileClasspath'
      exit 1
    fi
```

- [ ] **Step 4: Add the boundary check to build-platform regression coverage**

Add to `.github/workflows/durex-build-platform.yml` after the Music support-module check:

```yaml
- name: Spring runtime boundary contract
  run: bash migration/spring-music/verify-runtime-boundary.sh
```

- [ ] **Step 5: Verify GREEN**

Run:

```bash
bash migration/spring-music/verify-runtime-boundary.sh
gradle -p migration/spring-music :music:dependencies --configuration compileClasspath --stacktrace
```

Expected: no legacy CDI/inject/interceptor/transaction dependency in the active Spring compile classpath.

- [ ] **Step 6: Commit**

```bash
git add -- \
  migration/spring-music/verify-runtime-boundary.sh \
  .github/workflows/spring-music.yml \
  .github/workflows/durex-build-platform.yml
git commit -m "ci: enforce Spring runtime boundary"
```

---

### Task 5: Verify Phase A as a stable foundation and document the exit condition

**Files:**
- Modify: `docs/superpowers/specs/2026-08-21-durex-spring-boot-platform-v3-design.md`
- Test: existing Durex Build Platform, Plugin API, Spring Music, Spring Native, and transitional Quarkus workflows.

**Interfaces:**
- Consumes: all Phase A changes.
- Produces: a verified checkpoint suitable for Phase B HTTP work.

- [ ] **Step 1: Run focused local verification**

Run:

```bash
gradle -p build-logic/tests/transaction-feature-smoke verifyTransactionCapability --stacktrace
gradle -p build-logic/tests/aop-feature-smoke verifyAopCapability --stacktrace
bash build-logic/tests/verify-plugin-namespaces.sh
bash migration/spring-music/verify-runtime-boundary.sh
gradle -p migration/spring-music :music:durexDoctor :music:compileJava :music:test --configuration-cache --stacktrace
```

Expected: all commands PASS.

- [ ] **Step 2: Verify configuration-cache reuse for the migrated Spring graph**

Run twice:

```bash
gradle -p migration/spring-music :music:durexDoctor :music:test --configuration-cache --stacktrace
gradle -p migration/spring-music :music:durexDoctor :music:test --configuration-cache --stacktrace
```

Expected on second run: `Reusing configuration cache.`

- [ ] **Step 3: Run transitional legacy compilation once**

Until the final Quarkus deletion lands, run:

```bash
./gradlew :core:schema:music:repo:compileJava :core:music:compileJava --stacktrace
```

Expected: PASS through the thin `src/quarkus/java` wrappers. Do not add new feature behavior to make this path richer.

- [ ] **Step 4: Update the architecture spec with Phase A status**

Add a concise status section:

```markdown
## Implementation status

### Phase A — Runtime foundation

Implemented when the Spring Music graph:

- contains no CDI / `javax.inject` / `javax.interceptor` / `javax.transaction` runtime dependency;
- uses `durex.feature.transaction` for transaction semantics;
- constructs repositories/services through Spring wiring and Spring transaction AOP;
- preserves Durex semantic AOP tests from #132;
- passes the explicit Spring runtime boundary guard.

The remaining `src/quarkus/java` wrappers are transitional deletion targets, not supported platform APIs.
```

- [ ] **Step 5: Push and inspect PR CI**

Required successful PR workflows:

```text
Durex Plugin API
Durex Build Platform
Spring Music Migration
Spring Native Reference
```

If the transitional Quarkus workflow is still present on the execution base, it should also remain green until the workflow is deliberately removed in the legacy-deletion phase.

- [ ] **Step 6: Commit final Phase A checkpoint**

```bash
git add -- docs/superpowers/specs/2026-08-21-durex-spring-boot-platform-v3-design.md
git commit -m "docs: mark Spring runtime foundation complete"
```

---

## Self-Review Notes

- **Spec coverage:** Phase A covers transaction capability, removal of CDI/inject/transaction from the Spring graph, Spring-native construction, auto-configuration/AOP regression, and an explicit no-CDI CI guard. HTTP and messaging remain deferred to their own phases.
- **No hidden runtime rewrite:** Spring owns AOP/transaction infrastructure; Durex only composes dependencies and wiring conventions.
- **No business-code duplication:** temporary Quarkus wrappers subclass neutral implementations and contain no business logic.
- **Type consistency:** Spring beans are exposed under the existing `MusicRepository`, `PlayListRepository`, `PlayListMusicRepository`, `CreatorPlayListRepository`, `MusicService`, and `PlaylistService` types; existing controllers/tests do not require API renames.
- **Migration safety:** active Spring source paths become free of legacy Java EE/CDI imports while the old root can compile through isolated wrappers until final deletion.
- **Deliberate deferral:** HTTP contracts, HTTP client proxying, messaging listeners/forwarding, JSON schema plugin work, root Gradle modernization, and complete Quarkus deletion are not part of this implementation plan.
