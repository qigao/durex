#!/usr/bin/env bash
set -euo pipefail

publish_workflow=.github/workflows/durex-release.yml
verify_workflow=.github/workflows/durex-release-verify.yml

fail() {
  echo "::error::$*" >&2
  exit 1
}

[[ -f "$publish_workflow" ]] || fail "missing privileged release workflow: $publish_workflow"
[[ -f "$verify_workflow" ]] || fail "missing unprivileged release verification workflow: $verify_workflow"

if grep -Eq '^[[:space:]]{2}(pull_request|workflow_dispatch):' "$publish_workflow"; then
  fail "privileged Durex Release workflow must be tag-only"
fi
grep -Eq '^[[:space:]]{2}push:' "$publish_workflow" || fail "privileged release workflow must have a push trigger"
grep -Fq "- 'v*.*.*'" "$publish_workflow" || fail "privileged release workflow must be restricted to vX.Y.Z tags"

if grep -Fq 'contents: write' "$verify_workflow"; then
  fail "release verification workflow must not request contents: write"
fi
grep -Fq 'contents: read' "$verify_workflow" || fail "release verification workflow must explicitly request contents: read"
grep -Eq '^[[:space:]]{2}pull_request:' "$verify_workflow" || fail "release verification must run on pull requests"
grep -Eq '^[[:space:]]{2}workflow_dispatch:' "$verify_workflow" || fail "release verification must support manual dry-runs"

if grep -Eq 'DUREX_SIGNING_|CENTRAL_TOKEN_|secrets\.' "$verify_workflow"; then
  fail "release verification workflow must not reference release credentials"
fi

for workflow in "$publish_workflow" "$verify_workflow"; do
  mutable_refs=$(grep -En 'uses:[[:space:]]+[^[:space:]]+@v[0-9]+' "$workflow" || true)
  if [[ -n "$mutable_refs" ]]; then
    echo "$mutable_refs" >&2
    fail "release workflows must pin third-party actions to immutable commit SHAs"
  fi

done

echo "Durex release security boundary is explicit: PR verification is read-only/secret-free and publication is tag-only."
