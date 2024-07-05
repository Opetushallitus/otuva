#!/usr/bin/env bash
set -o errexit -o nounset -o pipefail

cluster_arn="$( aws ecs describe-clusters --clusters $ECS_CLUSTER_NAME --query "clusters[0].clusterArn" --output text )"
task_arn="$( aws ecs list-tasks --cluster "${cluster_arn}" --query 'taskArns[0]' --output text )"
container_id=$( aws ecs list-tasks --cluster "${cluster_arn}" --query 'taskArns[0]' --output text | cut -d '/' -f 3 )
container_runtime_id="$( aws ecs describe-tasks --cluster "${cluster_arn}" --tasks "${task_arn}" --query "tasks[0].containers[?name == 'AppContainer'] | [0].runtimeId" --output text )"

dbhost="$(aws secretsmanager get-secret-value --secret-id "${DB_SECRET}" --query 'SecretString' --output text | jq -r '.host')"
dbport="$(aws secretsmanager get-secret-value --secret-id "${DB_SECRET}" --query 'SecretString' --output text | jq -r '.port')"

# socat is used to forward port 1111 to 1112 which AWS CLI is listening on. AWS
# CLI listens on localhost:1112 so direct connection from outside the docker
# container is not possible :(
socat tcp-listen:1111,reuseaddr,fork tcp:localhost:1112 &

aws ssm start-session \
  --target "ecs:${ECS_CLUSTER_NAME}_${container_id}_${container_runtime_id}" \
  --document-name AWS-StartPortForwardingSessionToRemoteHost \
  --parameters "{\"host\":[\"${dbhost}\"],\"portNumber\":[\"${dbport}\"],\"localPortNumber\":[\"1112\"]}"
