#!/usr/bin/env bash
set -o errexit -o nounset -o pipefail
readonly repo="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

function main {
  cd "$repo"
  docker build -t service-provider .
  sp=$(cat $repo/metadata/hakasp.xml)
  keystore=$(base64 -i $repo/keystore/keystore.jks)
  docker run -it -p 8080:8080 -e ENV=local -e hakasp="$sp" -e keystore="$keystore" \
    -e ssm_keystore_password=m4JNeOMhcI42psiRyMS5 \
    -e ssm_app_username_to_usermanagement=username \
    -e ssm_app_password_to_usermanagement=password \
    -e ssm_sp_keyalias=luokka_hakasp_selfsigned \
    -e ssm_sp_keyalias_secondary=luokka_hakasp_selfsigned \
    -e ssm_sp_keypassword=61rj9jtBPCwqe9cfbTwr \
    -e ssm_mpassid_keyalias=local_mpassidtestsp_selfsigned_2023 \
    --name service-provider-container service-provider
}

main "$@"
