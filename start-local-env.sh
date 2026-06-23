#!/usr/bin/env bash
set -o errexit -o nounset -o pipefail -o xtrace
readonly repo="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

function stop() {
  cd "$repo"
  docker compose down
}
trap stop EXIT

function main {
  commands=(
    "$repo/scripts/run-docker-compose.sh"
    "$repo/scripts/run-cas-virkailija.sh"
    "$repo/scripts/run-cas-virkailija.sh"
    "$repo/scripts/run-cas-oppija.sh"
    "$repo/scripts/run-kayttooikeus.sh"
    "$repo/scripts/run-mock-substance-service.sh"
  )

  cd "$repo"
  local -r session="$( basename "$( pwd )" )"
  tmux kill-session -t "$session" || true
  tmux start-server
  tmux new-session -d -s "$session"
  for c in "${commands[@]}"; do
    tmux splitw -v
    tmux send-keys "$c" C-m
    tmux select-layout tiled
  done
  tmux kill-pane -t 0
  tmux select-layout tiled
  tmux attach-session -t "$session"
}

main "$@"
