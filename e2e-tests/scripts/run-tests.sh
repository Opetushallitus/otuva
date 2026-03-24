#!/usr/bin/env bash
set -o errexit -o nounset -o pipefail
readonly repo="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && cd ../.. && pwd )"
node_version=$( cat "$repo/.nvmrc" )

function main {
  cd "$repo"

  init_nodejs

  cd "$repo/e2e-tests"
  npm install
  npx playwright install-deps
  npx playwright install chromium
  npx playwright test "$@"
}

function init_nodejs {
  export NVM_DIR="${NVM_DIR:-$HOME/.cache/nvm}"
  set +o errexit
  source "$repo/scripts/lib/nvm.sh"
  nvm use "${node_version}" || nvm install "${node_version}"
  set -o errexit
}

main "$@"