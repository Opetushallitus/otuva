#!/usr/bin/env bash
set -o errexit -o nounset -o pipefail
source "$( dirname "${BASH_SOURCE[0]}" )/../lib/common-functions.sh"

function main {
  cd "$repo/cas-virkailija"
  select_java_version "25"
  ./gradlew clean build test
}

main "$@"
