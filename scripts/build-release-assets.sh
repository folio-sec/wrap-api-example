#!/usr/bin/env bash

set -euo pipefail

if [ "$#" -ne 1 ]; then
  echo "usage: $0 <output-directory>" >&2
  exit 2
fi

repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
output_directory="$1"

mkdir -p "$output_directory"
output_directory="$(cd "$output_directory" && pwd)"

staging_directory="$(mktemp -d)"
trap 'rm -rf "$staging_directory"' EXIT

languages=(golang java17 java8 php python ruby scala typescript)
assets=()

# Copy only the files git tracks for the language directory. Build
# outputs, dependency trees, and editor state are never tracked, so this
# keeps them out of the release without maintaining a list of patterns.
copy_tracked_files() {
  local source_directory="$1"
  local destination_directory="$2"

  mkdir -p "$destination_directory"
  git -C "$source_directory" ls-files -z \
    | tar -C "$source_directory" --null -T - -cf - \
    | tar -C "$destination_directory" -xf -
}

for language in "${languages[@]}"; do
  template_parent="${staging_directory}/template-${language}"
  assembled_parent="${staging_directory}/assembled-${language}"
  mkdir -p "$template_parent" "$assembled_parent"
  copy_tracked_files "${repo_root}/${language}" "${template_parent}/${language}"

  "${repo_root}/scripts/verify-interview-patch.sh" \
    "$language" "${template_parent}/${language}"

  cp -R "${template_parent}/${language}" "${assembled_parent}/${language}"

  template_asset="${language}-template.zip"
  assembled_asset="${language}.zip"
  patch_asset="${language}.patch"

  (
    cd "$template_parent"
    zip -q -r "${output_directory}/${template_asset}" "${language}/"
  )

  "${repo_root}/scripts/apply-interview-patch.sh" \
    "$language" "${assembled_parent}/${language}"

  (
    cd "$assembled_parent"
    zip -q -r "${output_directory}/${assembled_asset}" "${language}/"
  )

  cp "${repo_root}/patches/${language}.patch" \
    "${output_directory}/${patch_asset}"

  assets+=("$assembled_asset" "$template_asset" "$patch_asset")
done

(
  cd "$output_directory"
  sha256sum "${assets[@]}" > SHA256SUMS
)

echo "Release assets created in ${output_directory}"
