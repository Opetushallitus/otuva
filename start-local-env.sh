#!/usr/bin/env bash
set -o errexit -o nounset -o pipefail -o xtrace
readonly repo="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

function main {
  cd "$repo"
  local -r session="$( basename "$( pwd )" )"
  tmux kill-session -t "$session" || true
  tmux start-server
  tmux new-session -d -s "$session"
  
  tmux select-pane -t 0
  tmux splitw -h
  tmux select-pane -t 0
  tmux splitw -v
  tmux select-pane -t 2
  tmux splitw -v
  # +---+---+
  # | 0 | 2 |
  # +---+---+
  # | 1 | 3 |
  # +---+---+

  tmux select-pane -t 0
  tmux send-keys "$repo/scripts/run-docker-compose.sh" C-m

  tmux select-pane -t 1
  tmux send-keys "$repo/scripts/run-cas-virkailija.sh" C-m

  tmux select-pane -t 2
  tmux send-keys "$repo/scripts/run-cas-oppija.sh" C-m

  tmux select-pane -t 3
  tmux send-keys "$repo/scripts/run-kayttooikeus.sh" C-m

  tmux attach-session -t "$session"
}

main "$@"
