readonly KEYSTORE_SECRET_ID="/service-provider/Keystore"
readonly KEYSTORE_PASSWORD_PARAM="/service-provider/KeystorePassword"

function add_signing_key {
  local -r env="$1"
  local -r alias="$2"
  local -r cert_file="$3"

  require_docker
  require_command keytool
  require_command base64

  if [ ! -f "${cert_file}" ]; then
    fatal "Certificate file not found: ${cert_file}"
  fi

  export_aws_credentials "${env}"

  local -r workdir="$(mktemp -d)"
  # shellcheck disable=SC2064
  trap "rm -rf '${workdir}'" EXIT

  local -r keystore="${workdir}/${env}-keystore.jks"

  info "Fetching keystore from Secrets Manager (${KEYSTORE_SECRET_ID})"
  cd "${workdir}"
  aws secretsmanager get-secret-value \
    --secret-id "${KEYSTORE_SECRET_ID}" \
    --output text \
    --query SecretString \
    | base64 --decode > "${keystore}"

  info "Fetching keystore password from SSM (${KEYSTORE_PASSWORD_PARAM})"
  local -r password="$(aws ssm get-parameter \
    --name "${KEYSTORE_PASSWORD_PARAM}" \
    --with-decryption \
    --output text \
    --query Parameter.Value)"

  info "Aliases currently in keystore:"
  keytool -list -keystore "${keystore}" -storepass "${password}" -v \
    | grep -E '^Alias name:' >&2 || true

  if keytool -list -keystore "${keystore}" -storepass "${password}" -alias "${alias}" >/dev/null 2>&1; then
    fatal "Alias '${alias}' already exists in the ${env} keystore. Choose a different alias or remove the existing one first."
  fi

  info "Importing certificate '${cert_file}' as alias '${alias}'"
  keytool -importcert \
    -keystore "${keystore}" \
    -storepass "${password}" \
    -alias "${alias}" \
    -file "${cert_file}" \
    -noprompt

  info "Aliases after import:"
  keytool -list -keystore "${keystore}" -storepass "${password}" -v \
    | grep -E '^Alias name:' >&2

  info "Ready to upload modified keystore to Secrets Manager in env '${env}'."
  info "This will create a new version of '${KEYSTORE_SECRET_ID}'."
  read -r -p "Type 'yes' to confirm upload to ${env}: " confirm
  if [ "${confirm}" != "yes" ]; then
    fatal "Upload cancelled by user."
  fi

  local -r encoded="$(base64 < "${keystore}")"

  info "Uploading new keystore version to Secrets Manager"
  aws secretsmanager put-secret-value \
    --secret-id "${KEYSTORE_SECRET_ID}" \
    --secret-string "${encoded}" \
    --output text \
    --query 'VersionId' >&2

  info "Done. New version of ${KEYSTORE_SECRET_ID} published in env ${env}."
}
