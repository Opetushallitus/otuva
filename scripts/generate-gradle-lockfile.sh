#!/usr/bin/env bash
set -o errexit -o nounset -o pipefail
source "$( dirname "${BASH_SOURCE[0]}" )/lib/common-functions.sh"

function main {
  select_java_version "25"
  cd "${repo}/cas-oppija"
  ./gradlew dependencies --write-locks

  cd "${repo}/cas-virkailija"
  ./gradlew dependencies --write-locks
}

main "$@"
