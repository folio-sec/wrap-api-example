#!/usr/bin/env bash
#
# order scenario テストは「リバランス後の Toyopa 保有額が 19,000 円ではなく
# 20,000 円になる」という既知の不具合を再現したまま残してある。
# このスクリプトは CI で以下を確認する。
#
#   1. order scenario テストが失敗すること(通ってしまったら CI を失敗させる)
#   2. 期待した箇所(リバランス後の保有額の検証)で失敗していること
#
# Usage:
#   EXPECTED_FAILURE="<substring>" expect-order-scenario-failure.sh <command> [args...]
#
# EXPECTED_FAILURE は改行区切りで複数指定でき、そのすべてが出力に含まれている
# 必要がある。
set -uo pipefail

if [ $# -eq 0 ]; then
  echo "usage: EXPECTED_FAILURE=<substring> $0 <command> [args...]" >&2
  exit 2
fi

output_file="$(mktemp)"
trap 'rm -f "${output_file}"' EXIT

echo "::group::order scenario test output (failing on purpose)"
"$@" 2>&1 | tee "${output_file}"
status="${PIPESTATUS[0]}"
echo "::endgroup::"

if [ "${status}" -eq 0 ]; then
  echo "::error::order scenario test passed unexpectedly. This test is expected to fail at the rebalance assertion (19000 != 20000)."
  exit 1
fi

while IFS= read -r expected; do
  if [ -z "${expected}" ]; then
    continue
  fi
  if ! grep -qF -- "${expected}" "${output_file}"; then
    echo "::error::order scenario test failed, but not at the expected assertion. Missing message: ${expected}"
    exit 1
  fi
done <<< "${EXPECTED_FAILURE}"

echo "OK: order scenario test failed at the expected assertion (exit status ${status})."
