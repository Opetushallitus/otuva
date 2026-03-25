#!/usr/bin/env bash
set -o errexit -o nounset -o pipefail
source "$( dirname "${BASH_SOURCE[0]}" )/../lib/common-functions.sh"

trap cleanup EXIT INT TERM

function main {
  local params_for_playwright="${1:-}"
  require_docker

  start_keycloak
  start_mock_homepage
  start_kayttooikeus
  start_cas_oppija

  run_playwright_tests "$params_for_playwright"
}

function start_keycloak {
  pushd "$repo"/cas-oppija
  cp ./keycloak/localkeystore.jks ./config
  popd

  start_container cas-oppija/docker-compose.keycloak.yml
}

function stop_keycloak {
  stop_container cas-oppija/docker-compose.keycloak.yml
}

function start_mock_homepage {
  start_container mock-homepage/docker-compose.yml
}

function stop_mock_homepage {
  stop_container mock-homepage/docker-compose.yml
}

kayttooikeus_backend_pid=""

function start_kayttooikeus {
  start_container kayttooikeus-service/docker-compose.yml

  select_java_version "21"
  cd "$repo"/kayttooikeus-service

  ./mvnw clean install -Dmaven.test.skip=true
  wait_for_container_to_be_healthy kayttooikeus-db

  nohup java -Dspring.config.additional-location=classpath:/config/local.yml \
    --add-opens java.base/java.lang=ALL-UNNAMED \
    --add-opens java.base/java.util=ALL-UNNAMED \
    -jar target/kayttooikeus.jar &
  kayttooikeus_backend_pid=$!

  wait_for_backend_to_be_healthy kayttooikeus-service 8101
}

function wait_for_backend_to_be_healthy {
  local name=$1
  local port=$2
info "Waiting for backend ${name} to start on port ${port}"
  local -r max_wait=120
  local waited=0
  until curl -sf "http://localhost:${port}/${name}/actuator/health" >/dev/null 2>&1; do
    if [ $waited -ge $max_wait ]; then
      fatal "Backend ${name} did not start within ${max_wait}s"
    fi
    sleep 2
    waited=$((waited + 2))
  done
  info "Backend ${name} is ready"
}

function stop_kayttooikeus {
  stop_process kaytoikeus_backend $kayttooikeus_backend_pid
  stop_container kayttooikeus-service/docker-compose.yml
}

cas_oppija_backend_pid=""

function start_cas_oppija {
  start_container cas-oppija/docker-compose.dependencies.yml

  select_java_version "11"
  cd "$repo"/cas-oppija

  ./gradlew clean build -x test --no-daemon
  wait_for_container_to_be_healthy cas-oppija-postgres

  java -Dcas.standalone.configurationFile=config/local.yml -jar build/libs/cas.war &
  cas_oppija_backend_pid=$!

  wait_for_backend_to_be_healthy cas-oppija 8081
}

function stop_cas_oppija {
  stop_process cas_oppija_backend $cas_oppija_backend_pid
  stop_container cas-oppija/docker-compose.dependencies.yml
}

function select_java_version {
  if is_running_on_codebuild; then
    info "Running on CodeBuild; Java version is managed in buildspec"
  elif is_running_on_github_actions; then
    info "Running on Github actions; Java version is managed by actions/setup-java"
  else
    info "Switching to Java $1"
    java_version="$1"
    JAVA_HOME="$(/usr/libexec/java_home -v "${java_version}")"
    export JAVA_HOME
  fi
  java -version
}

function is_running_on_codebuild {
  [ -n "${CODEBUILD_BUILD_ID:-}" ]
}

function is_running_on_github_actions {
  [ -n "${GITHUB_ACTIONS:-}" ]
}

function start_container {
  local compose_file="$repo"/$1
  docker compose --file "$compose_file" up --force-recreate --renew-anon-volumes --detach
}

function stop_container {
  local compose_file="$repo"/$1
  docker compose --file $compose_file down --volumes --remove-orphans
}

function stop_process {
  local name=$1
  local pid=${2:-}

  if [ -n "$pid" ]; then
    info "Stopping proces $name (pid $pid)"
    kill "$pid" 2>/dev/null || true
    wait "$pid" 2>/dev/null || true
  fi
}

function cleanup {
  stop_cas_oppija
  stop_mock_homepage
  stop_kayttooikeus
  stop_keycloak
}

function wait_for_container_to_be_healthy {
  local -r container_name="$1"
  local -r timeout_seconds="${2:-60}"
  local -r start_time=$(date +%s)

  info "Waiting for docker container $container_name to be healthy (timeout: ${timeout_seconds}s)"
  until [ "$(docker inspect -f {{.State.Health.Status}} "$container_name" 2>/dev/null || echo "not-running")" == "healthy" ]; do
    local current_time=$(date +%s)
    local elapsed_time=$((current_time - start_time))

    if [ "$elapsed_time" -ge "$timeout_seconds" ]; then
      local status=$(docker inspect -f {{.State.Health.Status}} "$container_name" 2>/dev/null || echo "not-running")
      local logs=$(docker logs "$container_name" --tail 20 2>&1)
      fatal "Timed out waiting for container $container_name to be healthy. Current status: $status. Last logs:\n$logs"
    fi

    sleep 2
  done
}

function run_playwright_tests {
  local params_for_playwright="$1"
  cd "$repo"/e2e-tests
  init_nodejs
  npm_ci_if_needed
  npx playwright install --with-deps
  CI=true npx playwright test "$params_for_playwright"
}

main "$@"