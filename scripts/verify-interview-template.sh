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

if [ ! -d "$template_directory" ]; then
  echo "template directory not found: $template_directory" >&2
  exit 1
fi

if [ ! -f "${template_directory}/README.md" ]; then
  echo "README.md not found in template: $template_directory" >&2
  exit 1
fi

if grep -q '^## サービス概要$' "${template_directory}/README.md"; then
  echo "template README contains the interview description" >&2
  exit 1
fi

if [ -e "${template_directory}/${order_scenario}" ]; then
  echo "template contains OrderScenario: $order_scenario" >&2
  exit 1
fi

if [ -e "${template_directory}/${rebalance_usecase}" ]; then
  echo "template contains the rebalance use case: $rebalance_usecase" >&2
  exit 1
fi

# Scan every file that the template's own .gitignore files do not ignore,
# whether or not the directory is a git repository. Build outputs such as
# python/build/lib (created by a non-editable pip install) can hold copies of
# the interview code and must not cause false positives; untracked but
# unignored files are still scanned because they would ship in an archive.
grep_status=0
matches="$(
  cd "$template_directory" \
    && git grep --no-index --exclude-standard -n -i -I -E \
      'rebalance|リバランス' -- .
)" || grep_status=$?

if [ "$grep_status" -eq 0 ]; then
  printf '%s\n' "$matches" >&2
  echo "template contains rebalance-related content" >&2
  exit 1
elif [ "$grep_status" -ne 1 ]; then
  echo "git grep failed with status ${grep_status}" >&2
  exit 1
fi

echo "OK: ${language} template contains only pre-interview content."
