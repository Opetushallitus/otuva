#!/usr/bin/env bash
set -o errexit -o nounset -o pipefail
readonly repo="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && cd ../.. && pwd )"

trap "docker compose down" EXIT

function main {
  cd "$repo/kayttooikeus-service"
  docker compose up -d

  if is_running_on_codebuild; then
    "${repo}"/kayttooikeus-service/mvnw clean install -s ./settings.xml
  else
    "${repo}"/kayttooikeus-service/mvnw clean install
  fi
}

function is_running_on_codebuild {
  [ -n "${CODEBUILD_BUILD_ID:-}" ]
}

main "$@"
