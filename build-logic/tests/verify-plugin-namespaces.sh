#!/usr/bin/env bash
set -euo pipefail

repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
cd "$repo_root"

fail() {
  echo "plugin namespace contract failed: $*" >&2
  exit 1
}

require_file() {
  [[ -f "$1" ]] || fail "missing file $1"
}

reject_file() {
  [[ ! -e "$1" ]] || fail "legacy file still exists: $1"
}

require_text() {
  local file="$1"
  local text="$2"
  grep -Fq "$text" "$file" || fail "$file does not contain: $text"
}

reject_text() {
  local file="$1"
  local text="$2"
  if grep -Fq "$text" "$file"; then
    fail "$file still contains legacy text: $text"
  fi
}

reject_pattern() {
  local file="$1"
  local pattern="$2"
  if grep -Eq "$pattern" "$file"; then
    fail "$file still matches legacy pattern: $pattern"
  fi
}

# Local Durex plugin implementation remains temporarily for its own regression suite.
require_text build-bootstrap/build.gradle.kts 'id = "durex.settings"'
require_text build-logic/build.gradle.kts 'id = "durex.module"'

for file in \
  build-logic/src/main/groovy/durex.java-library.gradle \
  build-logic/src/main/groovy/durex.spring-library.gradle \
  build-logic/src/main/groovy/durex.spring-service.gradle \
  build-logic/src/main/groovy/durex.schema.jooq.gradle \
  build-logic/src/main/groovy/durex.schema.json.gradle \
  build-logic/src/main/groovy/durex.feature.aop.gradle \
  build-logic/src/main/groovy/durex.feature.transaction.gradle \
  build-logic/src/main/groovy/durex.feature.web.gradle \
  build-logic/src/main/groovy/durex.feature.http-client.gradle \
  build-logic/src/main/groovy/durex.feature.messaging.gradle \
  build-logic/src/main/groovy/durex.feature.jdbc.gradle \
  build-logic/src/main/groovy/durex.feature.jooq.gradle \
  build-logic/src/main/groovy/durex.feature.jpa.gradle \
  build-logic/src/main/groovy/durex.feature.redis.gradle \
  build-logic/src/main/groovy/durex.feature.native.gradle \
  build-logic/src/main/groovy/durex.feature.lombok.gradle; do
  require_file "$file"
done

# Internal bootstrap/composition plugins.
require_text build-bootstrap/build.gradle.kts 'id = "durex.internal.build-logic-settings"'
require_text build-bootstrap/build.gradle.kts 'id = "durex.internal.build-logic"'
require_text build-logic/build.gradle.kts 'id = "durex.internal.catalog"'
require_text build-logic/build.gradle.kts 'id = "durex.internal.fixture"'
require_file build-logic/src/main/groovy/durex.internal.java-base.gradle
require_file build-logic/src/main/groovy/durex.internal.spring-base.gradle

# Legacy precompiled plugin IDs must disappear rather than remain as aliases.
reject_file build-logic/src/main/groovy/durex.java-base.gradle
reject_file build-logic/src/main/groovy/durex.spring-base.gradle
reject_file build-logic/src/main/groovy/durex.jooq-schema.gradle

reject_text build-bootstrap/build.gradle.kts 'id = "durex.build-logic-settings"'
reject_text build-bootstrap/build.gradle.kts 'id = "durex.build-logic"'
reject_text build-logic/build.gradle.kts 'id = "durex.catalog"'
reject_text build-logic/build.gradle.kts 'id = "com.acme.durex.fixture"'

# Production builds consume only the two published SimpleDSL entry plugins.
require_text settings.gradle "id 'io.github.qigao.simpledsl.settings' version '0.1.0'"
reject_pattern settings.gradle 'includeBuild[[:space:]]*\('
reject_text settings.gradle "id 'durex.settings'"

while IFS= read -r file; do
  require_text "$file" 'io.github.qigao.simpledsl.build'
  reject_pattern "$file" "id[[:space:]]*\\(?[[:space:]]*['\"]durex\\."
done < <(find core -name 'build.spring.gradle' -type f | sort)

require_text core/schema/music/entity/build.spring.gradle 'SimpleDslJooqSchemaPlugin'
require_text core/schema/music/entity/build.spring.gradle 'simpledslJooq'
reject_text core/schema/music/entity/build.spring.gradle "id 'durex.schema.jooq'"

require_text core/schema/music/json/build.spring.gradle 'SimpleDslJsonSchemaPlugin'
require_text core/schema/music/json/build.spring.gradle 'simpledslJsonSchema'
reject_text core/schema/music/json/build.spring.gradle "id 'durex.schema.json'"
reject_text core/schema/music/json/build.spring.gradle "id 'org.jsonschema2pojo'"

for file in \
  migration/spring-messaging/settings.gradle \
  migration/spring-music/settings.gradle \
  reference/spring-capabilities/settings.gradle.kts \
  reference/spring-native/settings.gradle.kts; do
  require_text "$file" 'io.github.qigao.simpledsl.settings'
  reject_pattern "$file" 'includeBuild[[:space:]]*\('
  reject_pattern "$file" "id[[:space:]]*\\(?[[:space:]]*['\"]durex\\.settings"
done

echo 'Durex plugin namespace and SimpleDSL consumer contract: OK'
