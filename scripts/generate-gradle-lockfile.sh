#!/usr/bin/env bash
set -o errexit -o nounset -o pipefail
readonly repo="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && cd .. && pwd )"

function main {
  cd "${repo}/cas-oppija"
  use_java_version 11
  ./gradlew dependencies --write-locks

  cd "${repo}/cas-virkailija"
  use_java_version 21
  ./gradlew dependencies --write-locks
}

function use_java_version {
  JAVA_HOME="$( /usr/libexec/java_home -v "$1" )"
  export JAVA_HOME
}

main "$@"
