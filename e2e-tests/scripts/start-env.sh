#!/usr/bin/env bash
set -o errexit -o nounset -o pipefail
readonly repo="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && cd .. && pwd )"

function shutdown {
  echo "Shutting down all test services"
  stop_mock_nginx
  stop_cas_oppija_db
  stop_keycloak
}

trap shutdown EXIT

function main {
  cd "$repo"
  start_mock_nginx
  start_cas_oppija_db
  start_keycloak
  start_cas-oppija

  #./mock-homepage/scripts/run-nginx.sh
  #./cas-oppija/scripts/run-database.sh
  #./cas-oppija/scripts/run-keycloak.sh
  #./cas-oppija/scripts/run-cas-java-11.sh
}

function start_mock_nginx {
  cd "$repo"/../mock-homepage

  docker compose up -d
}

function stop_mock_nginx {
  cd "$repo"/../mock-homepage
  docker compose down
}

function start_cas_oppija_db {
  cd "$repo"/../cas-oppija
  docker compose -f docker-compose.dependencies.yml up -d --force-recreate --renew-anon-volumes
}

function stop_cas_oppija_db {
  cd "$repo"/../cas-oppija
  docker compose -f docker-compose.dependencies.yml down --volumes --remove-orphans
}

function start_keycloak {
  cd "$repo"/../cas-oppija
  docker compose -f docker-compose.keycloak.yml up -d --force-recreate --renew-anon-volumes
}

function stop_keycloak {
  cd "$repo"/../cas-oppija
  docker compose -f docker-compose.keycloak.yml down --volumes --remove-orphans
}

function start_cas-oppija {
  cd "$repo"/../cas-oppija
  ./scripts/run-cas-java-11.sh
}

main "$@"