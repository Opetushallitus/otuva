#!/usr/bin/env bash
set -o errexit -o nounset -o pipefail
readonly repo="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && cd .. && pwd )"

function main {
  cd "$repo"

  use_jvm_version "21"
  ./scripts/ci/run-tests-kayttooikeus.sh
  ./scripts/ci/run-tests-cas-virkailija.sh

  use_jvm_version "11"
  ./scripts/ci/run-tests-cas-oppija.sh
}

function use_jvm_version {
  JAVA_HOME="$( /usr/libexec/java_home -v "$1" )"
  export JAVA_HOME
}

main "$@"
