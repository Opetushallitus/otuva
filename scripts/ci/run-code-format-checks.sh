#!/usr/bin/env bash
set -o errexit -o nounset -o pipefail

repo="$(cd "$(dirname "${BASH_SOURCE[0]}")" && cd ../.. && pwd)"
source "${repo}/scripts/lib/common-functions.sh"

function main {
  run_prettier_check
}

function run_prettier_check {
  cd "$repo/infra"
  init_nodejs
  npm_ci_if_needed
  npx prettier . --check

  cd "$repo/e2e-tests"
  npm_ci_if_needed
  npm run prettier
}

main "$@"
