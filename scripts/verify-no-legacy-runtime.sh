#!/usr/bin/env bash
set -euo pipefail

fail=0

require_absent() {
  local path="$1"
  if [[ -e "$path" ]]; then
    echo "legacy runtime/build artifact still exists: $path"
    fail=1
  fi
}

require_absent core/music-reactive
require_absent core/shared/jakarta
require_absent core/myInterceptor
require_absent core/schema/music/r2dbc
require_absent core/springboot
require_absent integration-tests
require_absent gradle/library
require_absent gradle/versions
require_absent gradle/extensions
require_absent core/music/build.gradle
require_absent core/music/src/main/java/com/github/durex/music/controller
require_absent core/music/src/main/java/com/github/durex/music/config
require_absent core/music/src/test
require_absent core/schema/music/entity/build.gradle
require_absent core/schema/music/repo/build.gradle
require_absent core/schema/music/repo/src/test
require_absent core/schema/music/json/build.gradle
require_absent core/shared/utils/build.gradle

active_paths=(
  build-bootstrap
  build-logic
  core
  gradle/dependencies
  gradle/modules.toml
  migration
  reference
  settings.gradle
  build.gradle
)

pattern='io\.quarkus|javax\.enterprise|javax\.inject|javax\.interceptor|smallrye|org\.jboss\.jandex|qLibs'

existing=()
for path in "${active_paths[@]}"; do
  [[ -e "$path" ]] && existing+=("$path")
done

if ((${#existing[@]} > 0)); then
  matches=$(grep -R -n -E \
    --exclude='*.md' \
    --exclude='*.adoc' \
    --exclude='verify-no-legacy-runtime.sh' \
    "$pattern" "${existing[@]}" || true)
  if [[ -n "$matches" ]]; then
    echo "$matches"
    echo 'legacy Quarkus/CDI runtime reference leaked into active build/source graph'
    fail=1
  fi
fi

if ((fail)); then
  exit 1
fi

echo 'legacy runtime/build boundary: clean'
