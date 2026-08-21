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
  if grep -nE 'javax-(cdi|inject|interceptor|transaction|validation)|quarkus' "$file"; then
    fail "$file contains a legacy runtime dependency"
  fi
done

for path in \
  core/shared/common/src/main/java \
  core/shared/spring \
  core/music/src/main/java/com/github/durex/music/service \
  core/music/src/spring/java \
  core/schema/music/repo/src/main/java; do
  if grep -R -nE 'import javax\.(enterprise|inject|interceptor|transaction|validation)|io\.quarkus|org\.eclipse\.microprofile' "$path"; then
    fail "$path contains a legacy runtime import"
  fi
done

echo 'Spring runtime boundary: OK'
