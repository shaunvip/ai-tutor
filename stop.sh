#!/usr/bin/env bash

set -u

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

log() {
  printf '[ai-tutor-stop] %s\n' "$*"
}

has_command() {
  command -v "$1" >/dev/null 2>&1
}

stop_pids() {
  label="$1"
  pids="$2"

  if [ -z "$pids" ]; then
    log "$label: not running"
    return
  fi

  log "$label: stopping pids $pids"
  # shellcheck disable=SC2086
  kill -TERM $pids >/dev/null 2>&1 || true
  sleep 2

  survivors=""
  for pid in $pids; do
    if kill -0 "$pid" >/dev/null 2>&1; then
      survivors="$survivors $pid"
    fi
  done

  if [ -n "$survivors" ]; then
    log "$label: force killing pids$survivors"
    # shellcheck disable=SC2086
    kill -KILL $survivors >/dev/null 2>&1 || true
  fi
}

stop_port() {
  port="$1"
  label="$2"

  if ! has_command lsof; then
    log "$label: lsof not found, skipping port $port"
    return
  fi

  pids="$(lsof -ti "tcp:$port" 2>/dev/null | sort -u | tr '\n' ' ')"
  stop_pids "$label on port $port" "$pids"
}

stop_pattern() {
  pattern="$1"
  label="$2"

  if ! has_command pgrep; then
    log "$label: pgrep not found, skipping"
    return
  fi

  pids="$(pgrep -f "$pattern" 2>/dev/null | grep -v "^$$$" | sort -u | tr '\n' ' ')"
  stop_pids "$label" "$pids"
}

stop_docker_postgres() {
  if has_command docker && [ -f "$ROOT_DIR/docker-compose.yml" ]; then
    log "docker compose postgres: stopping"
    (cd "$ROOT_DIR" && docker compose stop postgres >/dev/null 2>&1) || true
  fi
}

stop_podman_containers() {
  if ! has_command podman; then
    log "podman: not installed"
    return
  fi

  for container in ai-tutor-ollama ai-tutor-postgres; do
    if podman container exists "$container" >/dev/null 2>&1; then
      log "podman container $container: stopping"
      podman stop "$container" >/dev/null 2>&1 || true
    else
      log "podman container $container: not found"
    fi
  done
}

stop_direct_ollama_model() {
  if has_command ollama; then
    log "ollama model gemma3: stopping if loaded"
    ollama stop gemma3 >/dev/null 2>&1 || true
  fi
}

stop_brew_postgres() {
  if has_command brew; then
    log "Homebrew postgresql@16: stopping if managed by brew services"
    brew services stop postgresql@16 >/dev/null 2>&1 || true
  fi
}

log "stopping AI Tutor local services"

stop_direct_ollama_model
stop_podman_containers
stop_docker_postgres
stop_brew_postgres

# App service ports.
stop_port 8080 "Spring Boot API"
stop_port 8000 "Python worker"
stop_port 8081 "Expo Metro"
stop_port 8082 "Expo Metro"
stop_port 19000 "Expo"
stop_port 19001 "Expo"
stop_port 19002 "Expo"
stop_port 11434 "Ollama"
stop_port 5432 "Postgres"

# Fallback process patterns for services that may not expose ports yet.
stop_pattern "uvicorn app.main:app" "Python uvicorn worker"
stop_pattern "mvn spring-boot:run" "Spring Boot Maven run"
stop_pattern "expo start" "Expo CLI"
stop_pattern "Metro Bundler" "Metro Bundler"

log "done"
