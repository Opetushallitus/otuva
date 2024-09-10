#!/usr/bin/env bash
set -o errexit -o nounset -o pipefail

readonly repo="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

function main {
  local -r ENV="$1"
  local -r VERSION="$2"
  local -r SERVICE="cas-oppija"

  init_cloud_base_virtualenv
  pull_cloud_environment_changes

  pushd "$repo/../cloud-base"
  info "./aws/config.py \"$ENV\" publish"
  ./aws/config.py "$ENV" publish

  info "./aws/cloudformation.py \"$ENV\" services update -s \"$SERVICE\" --force"
  ./aws/cloudformation.py "$ENV" services update -s "$SERVICE" --force

  info "Checking currently running version"
  info "./aws/deploy.py \"$ENV\" \"$SERVICE\" image"
  ./aws/deploy.py "$ENV" "$SERVICE" image

  info "Deploying version $VERSION to $ENV"
  info "./aws/deploy.py \"$ENV\" \"$SERVICE\" deploy --version \"$VERSION\" --force"
  ./aws/deploy.py "$ENV" "$SERVICE" deploy --version "$VERSION" --force
  popd
}

function init_cloud_base_virtualenv {
  info "Initializing cloud-base virtualenv"
  pushd "$repo/../cloud-base"
  git pull --rebase
  . oph-venv/bin/activate
  pip install -r requirements.txt
  popd
}

function pull_cloud_environment_changes {
  info "Checking changes to cloud-environment repo"
  pushd "$repo/../cloud-environment-$ENV"
  git pull --rebase
  popd
}

function info {
  log "INFO" "$1"
}

function fatal {
  log "ERROR" "$1"
  exit 1
}

function log {
  local -r level="$1"
  local -r message="$2"
  local -r timestamp=$(date +"%Y-%m-%d %H:%M:%S")

  >&2 echo -e "${timestamp} ${level} ${message}"
}

time main "$@"
