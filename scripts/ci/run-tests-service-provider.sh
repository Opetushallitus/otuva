#!/usr/bin/env bash
set -o errexit -o nounset -o pipefail
readonly repo="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && cd ../.. && pwd )"

function main {
  cd "$repo/service-provider"
  ./mvnw clean test
}

main "$@"
