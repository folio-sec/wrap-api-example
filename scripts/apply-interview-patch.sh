#!/usr/bin/env bash

set -euo pipefail

usage() {
  echo "usage: $0 <language> [template-directory]" >&2
  exit 2
}

if [ "$#" -lt 1 ] || [ "$#" -gt 2 ]; then
  usage
fi

language="$1"
case "$language" in
  golang | java8 | java17 | php | python | ruby | scala | typescript) ;;
  *)
    echo "unsupported language: $language" >&2
    exit 2
    ;;
esac

repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
target_directory="${2:-${repo_root}/${language}}"
patch_file="${repo_root}/patches/${language}.patch"

if [ ! -d "$target_directory" ]; then
  echo "template directory not found: $target_directory" >&2
  exit 1
fi

if [ ! -f "$patch_file" ]; then
  echo "patch file not found: $patch_file" >&2
  exit 1
fi

git -C "$target_directory" apply --no-index --check "$patch_file"
git -C "$target_directory" apply --no-index "$patch_file"
