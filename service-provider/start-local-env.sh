#!/usr/bin/env bash
set -o errexit -o nounset -o pipefail
readonly repo="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

function main {
  cd "$repo"
  docker build -t service-provider .
  sp=$(cat $repo/metadata/hakasp.xml)
  keystore=$(base64 -i $repo/keystore/keystore.jks)
  docker run -it -p 8080:8080 -e ENV=local -e hakasp="$sp" -e keystore="$keystore" --name service-provider-container service-provider
}

main "$@"
