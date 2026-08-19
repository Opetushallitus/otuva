#!/usr/bin/env bash
set -o errexit -o nounset -o pipefail
source "$( dirname "${BASH_SOURCE[0]}" )/lib/common-functions.sh"

function main {
  cd "$repo/kayttooikeus-service"

  select_java_version "21"
  wait_for_database

  local -r jvm_args=(
    "--add-opens=java.base/java.util=ALL-UNNAMED"
    "--add-opens=java.base/java.lang=ALL-UNNAMED"
    "-Dspring.config.additional-location=classpath:/config/local.yml"
  )

  ./mvnw clean install -DskipTests
  ./mvnw -Dspring-boot.run.jvmArguments="${jvm_args[*]}" spring-boot:run
}

function wait_for_database {
  while ! docker compose port kayttooikeus-db 5432 >&2; do
    echo "Waiting for database to start..."
    sleep 1
  done
}


main "$@"
