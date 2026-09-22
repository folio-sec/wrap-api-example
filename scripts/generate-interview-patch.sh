#!/usr/bin/env bash

set -euo pipefail

usage() {
  echo "usage: $0 <language> <assembled-directory> [output-patch]" >&2
  exit 2
}

if [ "$#" -lt 2 ] || [ "$#" -gt 3 ]; then
  usage
fi

language="$1"
assembled_directory="$2"
case "$language" in
  golang | java8 | java17 | php | python | ruby | scala | typescript) ;;
  *)
    echo "unsupported language: $language" >&2
    exit 2
    ;;
esac

repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
template_directory="${repo_root}/${language}"
manifest_file="${repo_root}/interview/manifests/${language}.txt"
output_patch="${3:-${repo_root}/patches/${language}.patch}"

if [ ! -d "$assembled_directory" ]; then
  echo "assembled directory not found: $assembled_directory" >&2
  exit 1
fi

if [ ! -f "$manifest_file" ]; then
  echo "manifest not found: $manifest_file" >&2
  exit 1
fi

"${repo_root}/scripts/verify-interview-template.sh" \
  "$language" "$template_directory"

tmp_directory="$(mktemp -d)"
trap 'rm -rf "$tmp_directory"' EXIT
worktree="${tmp_directory}/worktree"
mkdir "$worktree"
cp -R "${template_directory}/." "$worktree/"

git -C "$worktree" init -q
git -C "$worktree" config user.name interview-patch-generator
git -C "$worktree" config user.email interview-patch-generator@example.invalid
git -C "$worktree" add .
git -C "$worktree" commit -q -m template

find "$worktree" -mindepth 1 -maxdepth 1 ! -name .git \
  -exec rm -rf -- {} +
cp -R "${assembled_directory}/." "$worktree/"

generated_patch="${tmp_directory}/${language}.patch"
git -C "$worktree" add --intent-to-add .
# --no-renames keeps every change as a plain add/delete/modify so that the
# manifest check below sees each touched path in a `diff --git` header.
git -C "$worktree" diff --binary --no-renames -- . > "$generated_patch"

if [ ! -s "$generated_patch" ]; then
  echo "generated patch is empty" >&2
  exit 1
fi

if grep -q -E '^(rename|copy) (from|to) ' "$generated_patch"; then
  echo "generated patch contains rename/copy entries: $generated_patch" >&2
  exit 1
fi

awk '/^diff --git [ab]\// { path = $4; sub(/^[ab]\//, "", path); print path }' \
  "$generated_patch" | LC_ALL=C sort -u > "${tmp_directory}/actual-paths.txt"
LC_ALL=C sort -u "$manifest_file" > "${tmp_directory}/expected-paths.txt"

if ! diff -u "${tmp_directory}/expected-paths.txt" "${tmp_directory}/actual-paths.txt"; then
  echo "generated patch differs from ${manifest_file}; review and update the manifest first" >&2
  exit 1
fi

verification_directory="${tmp_directory}/verification"
mkdir "$verification_directory"
cp -R "${template_directory}/." "$verification_directory/"
git -C "$verification_directory" apply --check "$generated_patch"
git -C "$verification_directory" apply "$generated_patch"

if ! diff -qr -x .git "$assembled_directory" "$verification_directory"; then
  echo "generated patch does not reconstruct the assembled directory" >&2
  exit 1
fi

mkdir -p "$(dirname "$output_patch")"
mv "$generated_patch" "$output_patch"
echo "Generated ${output_patch}"
