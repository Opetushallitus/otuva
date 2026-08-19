#!/usr/bin/env bash
set -o errexit -o nounset -o pipefail
source "$( dirname "${BASH_SOURCE[0]}" )/lib/common-functions.sh"

function main {
  cd "$repo/mock-substance-service"

  select_java_version "21"

  ./mvnw clean install -Dmaven.test.skip=true
  ./mvnw spring-boot:run \
    -Dspring-boot.run.jvmArguments="-Dspring.config.additional-location=classpath:/config/local.yml --add-opens java.base/java.lang=ALL-UNNAMED --add-opens java.base/java.util=ALL-UNNAMED"
}

main "$@"
