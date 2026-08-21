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
    fail "$file still contains legacy plugin id: $text"
  fi
}

# Public bootstrap/project plugins.
require_text build-bootstrap/build.gradle.kts 'id = "durex.settings"'
require_text build-logic/build.gradle.kts 'id = "durex.module"'

# Public precompiled convention plugins.
for file in \
  build-logic/src/main/groovy/durex.java-library.gradle \
  build-logic/src/main/groovy/durex.spring-library.gradle \
  build-logic/src/main/groovy/durex.spring-service.gradle \
  build-logic/src/main/groovy/durex.schema.jooq.gradle \
  build-logic/src/main/groovy/durex.feature.aop.gradle \
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

require_text core/schema/music/entity/build.spring.gradle "id 'durex.schema.jooq'"
reject_text core/schema/music/entity/build.spring.gradle "id 'durex.jooq-schema'"

echo 'Durex public plugin namespace contract: OK'
