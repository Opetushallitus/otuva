#!/usr/bin/env bash
set -o errexit -o nounset -o pipefail
readonly repo="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && cd .. && pwd )"

function main {
  cd "$repo/kayttooikeus-service"

  wait_for_postgres
  use_correct_jvm_version

  local -r jvm_args=(
    "--add-opens=java.base/java.util=ALL-UNNAMED"
    "--add-opens=java.base/java.lang=ALL-UNNAMED"
    "-Dspring.config.additional-location=classpath:/config/local.yml"
  )

  ./mvnw clean install -DskipTests
  ./mvnw -Dspring-boot.run.jvmArguments="${jvm_args[*]}" spring-boot:run
}

function wait_for_postgres {
  while ! docker compose port kayttooikeus-db 5432 >&2; do
    echo "Waiting for database to start..."
    sleep 1
  done
}

function use_correct_jvm_version {
  JAVA_HOME="$( /usr/libexec/java_home -v "21" )"
  export JAVA_HOME
}

main "$@"
