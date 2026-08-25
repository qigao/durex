#!/usr/bin/env bash
set -euo pipefail

if [[ $# -ne 4 ]]; then
  echo "usage: $0 <generate|check> <staging-root> <surface-manifest> <signature-file>" >&2
  exit 2
fi

command=$1
staging_root=$2
manifest=$3
signature_file=$4

main_jar() {
  local artifact=$1
  find "$staging_root/$artifact/0.1.0-SNAPSHOT" -maxdepth 1 -type f \
    -name "${artifact}-*.jar" \
    ! -name '*-sources.jar' \
    ! -name '*-javadoc.jar' \
    -print -quit
}

generate_signatures() {
  local output=$1
  local unsorted
  unsorted=$(mktemp)
  trap 'rm -f "$unsorted"' RETURN

  while IFS='|' read -r artifact scope fqcn; do
    [[ "$scope" == "api" ]] || continue

    local jar_file
    jar_file=$(main_jar "$artifact")
    if [[ -z "$jar_file" ]]; then
      echo "missing staged jar for artifact: $artifact" >&2
      exit 1
    fi

    javap -classpath "$jar_file" -protected -s "$fqcn" \
      | sed -e '/^Compiled from /d' -e '/^[[:space:]]*$/d' -e 's/[[:space:]]*$//' \
      | while IFS= read -r line; do
          printf '%s|%s\n' "$fqcn" "$line"
        done >> "$unsorted"
  done < <(grep -vE '^[[:space:]]*(#|$)' "$manifest")

  LC_ALL=C sort -u "$unsorted" > "$output"
}

case "$command" in
  generate)
    generate_signatures "$signature_file"
    ;;
  check)
    if [[ ! -f "$signature_file" ]]; then
      echo "signature baseline does not exist: $signature_file" >&2
      exit 1
    fi
    current=$(mktemp)
    missing=$(mktemp)
    trap 'rm -f "$current" "$missing"' EXIT
    generate_signatures "$current"
    comm -23 <(LC_ALL=C sort -u "$signature_file") "$current" > "$missing"
    if [[ -s "$missing" ]]; then
      echo "::error::Public API compatibility baseline is not satisfied. Missing signatures:" >&2
      cat "$missing" >&2
      exit 1
    fi
    ;;
  *)
    echo "unknown command: $command" >&2
    exit 2
    ;;
esac
