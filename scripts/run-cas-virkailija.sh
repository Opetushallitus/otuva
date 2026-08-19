#!/usr/bin/env bash
set -o errexit -o nounset -o pipefail
source "$( dirname "${BASH_SOURCE[0]}" )/lib/common-functions.sh"

function main {
  cd "$repo/cas-virkailija"

  select_java_version "25"
  wait_for_database
  ./gradlew clean run -Dcas.standalone.configurationFile=config/local.yml
}

function wait_for_database {
  while ! docker compose port cas-virkailija-db 5432 &> /dev/null; do
    >&2 echo "Waiting for database to start..."
    sleep 1
  done
}

main "$@"
