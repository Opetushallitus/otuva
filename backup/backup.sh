#!/usr/bin/env bash
set -o errexit -o nounset -o pipefail

function main {
  mkdir -p /output
  PGPASSWORD="$DB_PASSWORD" pg_dump \
    --user "$DB_USERNAME" \
    --host $DB_HOSTNAME \
    --port $DB_PORT \
    --dbname $DB_NAME \
    --format custom \
    --file "/output/$DB_NAME.dump"
  ls -la /output
  aws s3 cp "/output/$DB_NAME.dump" "s3://$S3_BUCKET/$DB_NAME/$( date +%Y-%m-%d )-$DB_NAME.dump"
}

main
