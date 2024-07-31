#!/usr/bin/env bash
set -o errexit -o nounset -o pipefail

readonly dump_dir="/output"
readonly dump_file="$dump_dir/$DB_NAME.dump"

function main {
  create_dump

  local seconds_from_epoch
  seconds_from_epoch=$(date +%s)

  copy_dump_as_daily_backup "$seconds_from_epoch"
  copy_dump_as_monthly_backup "$seconds_from_epoch"
}

function create_dump {
  mkdir -p "$dump_dir"
  PGPASSWORD="$DB_PASSWORD" pg_dump \
    --user "$DB_USERNAME" \
    --host $DB_HOSTNAME \
    --port $DB_PORT \
    --dbname $DB_NAME \
    --format custom \
    --file "$dump_file"
  ls -la "$dump_dir"
}

function copy_dump_as_daily_backup {
  local seconds_from_epoch=$1
  local formatted_date
  formatted_date=$(date -d @"$seconds_from_epoch" +%Y-%m-%d)
  local s3_url="s3://$S3_BUCKET/daily/$DB_NAME/$formatted_date-$DB_NAME.dump"

  echo "Copying dump as daily backup file $s3_url"
  aws s3 cp "$dump_file" "$s3_url"
  log_daily_backup_success
  echo "Copied dump as daily backup file $s3_url"
}

function copy_dump_as_monthly_backup {
  local seconds_from_epoch=$1
  local formatted_date
  formatted_date=$(date -d @"$seconds_from_epoch" +%Y-%m)
  local key="monthly/$DB_NAME/$formatted_date-$DB_NAME.dump"
  local s3_url="s3://$S3_BUCKET/$key"

  echo "Checking whether monthly backup file $s3_url exists and has size > 0"
  local output=$(aws s3api head-object --bucket "$S3_BUCKET" --key "$key" 2>&1)

  if echo "$output" | grep -q 'ContentLength'; then
    local size
    size=$(echo "$output" | jq -r '.ContentLength')
    if [ "$size" -gt 0 ]; then
      echo "Monthly backup file $s3_url exists and has size > 0. Nothing to do."
      return 0
    else
      echo "Monthly backup file $s3_url exists but is empty"
    fi
  else
    echo "Monthly backup file $s3_url does not exist"
  fi

  echo "Copying dump as monthly backup file $s3_url"
  aws s3 cp "$dump_file" "$s3_url"
  log_monthly_backup_success
  echo "Copied dump as monthly backup file $s3_url"
}

function log_monthly_backup_success {
  log_backup_success "$DB_NAME" "monthly"
}

function log_daily_backup_success {
  log_backup_success "$DB_NAME" "daily"
}

function log_backup_success {
  local dbname=$1
  local frequency=$2
  local now
  now=$(date +"%Y-%m-%dT%H:%M:%S%z");
  echo "{\"timestamp\": \"$now\", \"msg\": \"success\", \"dbname\": \"$dbname\", \"frequency\": \"$frequency\"}"
}

main