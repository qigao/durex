#!/usr/bin/env bash
set -euo pipefail

if [[ $# -ne 4 ]]; then
  echo "usage: $0 <staging-repository> <version> <signed|unsigned> <output.zip>" >&2
  exit 2
fi

staging_repository=$1
version=$2
signature_mode=$3
output=$4

[[ "$version" =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]]
case "$signature_mode" in
  signed|unsigned) ;;
  *) echo "invalid signature mode: $signature_mode" >&2; exit 2 ;;
esac

artifacts=(shared-common shared-spring-http messaging-api messaging-spring-redis)
group_path=io/github/qigao/durex
source_root="$staging_repository/$group_path"
work=$(mktemp -d)
trap 'rm -rf "$work"' EXIT
bundle_root="$work/bundle"

mkdir -p "$bundle_root/$group_path"

for artifact in "${artifacts[@]}"; do
  source_dir="$source_root/$artifact/$version"
  target_dir="$bundle_root/$group_path/$artifact/$version"
  test -d "$source_dir"
  mkdir -p "$target_dir"

  files=(
    "$artifact-$version.jar"
    "$artifact-$version-sources.jar"
    "$artifact-$version-javadoc.jar"
    "$artifact-$version.pom"
  )

  for filename in "${files[@]}"; do
    source_file="$source_dir/$filename"
    target_file="$target_dir/$filename"
    test -f "$source_file"
    cp "$source_file" "$target_file"

    md5sum "$target_file" | awk '{print $1}' > "$target_file.md5"
    sha1sum "$target_file" | awk '{print $1}' > "$target_file.sha1"

    if [[ "$signature_mode" == signed ]]; then
      test -s "$source_file.asc"
      cp "$source_file.asc" "$target_file.asc"
    fi
  done

done

mkdir -p "$(dirname "$output")"
output=$(realpath -m "$output")
(
  cd "$bundle_root"
  zip -q -r "$output" io
)
unzip -tqq "$output"

echo "$output"
