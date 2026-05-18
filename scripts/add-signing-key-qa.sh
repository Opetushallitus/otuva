#!/usr/bin/env bash
set -o errexit -o nounset -o pipefail
source "$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )/lib/common-functions.sh"
source "${repo}/scripts/lib/service-provider-keystore.sh"

function main {
  if [ "$#" -ne 2 ]; then
    fatal "Usage: $(basename "$0") <alias> <certificate-file>"
  fi
  local -r env=$(parse_env_from_script_name)
  add_signing_key "${env}" "$1" "$2"
}

main "$@"
