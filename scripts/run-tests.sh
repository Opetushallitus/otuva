#!/usr/bin/env bash
set -o errexit -o nounset -o pipefail
source "$( dirname "${BASH_SOURCE[0]}" )/lib/common-functions.sh"

function main {
  cd "$repo"

  select_java_version "21"
  ./scripts/ci/run-tests-kayttooikeus.sh
  ./scripts/ci/run-tests-cas-virkailija.sh
}

main "$@"
