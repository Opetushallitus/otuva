#!/usr/bin/env bash
set -o errexit -o nounset -o pipefail

readonly repo="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

function main {
  local -r ENV="$1"
  shift

  AWS_PROFILE="oph-yleiskayttoiset-${ENV}" awslogs "$@"
}

function awslogs {
  docker build --tag awslogs - <<EOF
FROM python:3.11
RUN apt-get update && apt-get -y install
RUN pip3 install awslogs==0.14.0
ENTRYPOINT ["awslogs"]
EOF
  docker run --rm --interactive \
    --volume "${HOME}/.aws:/root/.aws" \
    --env AWS_REGION=eu-west-1 \
    --env AWS_PROFILE \
    awslogs "$@"
}

main "$@"
