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

target_directory="$(cd "$target_directory" && pwd)"

# Inside a git repository, `git apply` resolves patch paths against the
# repository root rather than the current directory, and silently skips
# ("Skipped patch ...", exit 0) every path that falls outside the current
# directory. Running from <repo>/golang would therefore apply nothing.
# When the target lives inside this repository, apply from the root with
# --directory so the paths are prefixed correctly.
case "${target_directory}/" in
  "${repo_root}/"*)
    relative_target="${target_directory#"${repo_root}"/}"
    git -C "$repo_root" apply --check \
      --directory="$relative_target" "$patch_file"
    git -C "$repo_root" apply \
      --directory="$relative_target" "$patch_file"
    ;;
  *)
    git -C "$target_directory" apply --check "$patch_file"
    git -C "$target_directory" apply "$patch_file"
    ;;
esac
