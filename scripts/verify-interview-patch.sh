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
template_directory="${2:-${repo_root}/${language}}"
patch_file="${repo_root}/patches/${language}.patch"
manifest_file="${repo_root}/interview/manifests/${language}.txt"
shared_readme="${repo_root}/interview/shared/README.challenge.md"

case "$language" in
  golang)
    order_scenario="test/order_scenario_test.go"
    rebalance_usecase="internal/application/usecase/order/rebalance_order_usecase.go"
    ;;
  java8 | java17)
    order_scenario="src/test/java/folio/codinginterview/OrderScenarioTest.java"
    rebalance_usecase="src/main/java/folio/codinginterview/application/usecase/order/RebalanceOrderUsecase.java"
    ;;
  php)
    order_scenario="tests/OrderScenarioTest.php"
    rebalance_usecase="src/CodingInterview/Application/Usecase/Order/RebalanceOrderUsecase.php"
    ;;
  python)
    order_scenario="tests/test_order_scenario.py"
    rebalance_usecase="src/coding_interview/application/usecase/order/rebalance_order_usecase.py"
    ;;
  ruby)
    order_scenario="spec/order_scenario_spec.rb"
    rebalance_usecase="lib/coding_interview/application/usecase/order/rebalance_order_usecase.rb"
    ;;
  scala)
    order_scenario="src/test/scala/folio/codinginterview/OrderScenario.scala"
    rebalance_usecase="src/main/scala/folio/codinginterview/application/usecase/order/RebalanceOrderUsecase.scala"
    ;;
  typescript)
    order_scenario="tests/orderScenario.test.ts"
    rebalance_usecase="src/application/usecase/order/rebalanceOrderUsecase.ts"
    ;;
esac

for required_file in "$patch_file" "$manifest_file" "$shared_readme"; do
  if [ ! -f "$required_file" ]; then
    echo "required file not found: $required_file" >&2
    exit 1
  fi
done

"${repo_root}/scripts/verify-interview-template.sh" "$language" "$template_directory"

tmp_directory="$(mktemp -d)"
trap 'rm -rf "$tmp_directory"' EXIT

awk '/^diff --git [ab]\// { path = $4; sub(/^[ab]\//, "", path); print path }' \
  "$patch_file" | LC_ALL=C sort -u > "${tmp_directory}/actual-paths.txt"
LC_ALL=C sort -u "$manifest_file" > "${tmp_directory}/expected-paths.txt"

if ! diff -u "${tmp_directory}/expected-paths.txt" "${tmp_directory}/actual-paths.txt"; then
  echo "patch changes files outside its manifest: $patch_file" >&2
  exit 1
fi

mkdir "${tmp_directory}/assembled"
cp -R "${template_directory}/." "${tmp_directory}/assembled/"

git -C "${tmp_directory}/assembled" apply --no-index --check "$patch_file"
git -C "${tmp_directory}/assembled" apply --no-index "$patch_file"

if [ ! -f "${tmp_directory}/assembled/${order_scenario}" ]; then
  echo "patch did not restore OrderScenario: $order_scenario" >&2
  exit 1
fi

if [ ! -f "${tmp_directory}/assembled/${rebalance_usecase}" ]; then
  echo "patch did not restore the rebalance use case: $rebalance_usecase" >&2
  exit 1
fi

awk '/^## サービス概要$/ { found = 1 } found' \
  "${tmp_directory}/assembled/README.md" > "${tmp_directory}/README.challenge.md"

if ! cmp -s "$shared_readme" "${tmp_directory}/README.challenge.md"; then
  echo "patch README content differs from interview/shared/README.challenge.md" >&2
  exit 1
fi

echo "OK: ${language} patch is scoped correctly and restores the interview content."
