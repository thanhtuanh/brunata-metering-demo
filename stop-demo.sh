#!/usr/bin/env bash
set -euo pipefail

# Stoppt die Demo-App und den DB-Container.
# Aufruf:
#   ./stop-demo.sh          -> App stoppen, DB stoppen (container bleibt)
#   ./stop-demo.sh --down   -> App stoppen, DB + Netzwerk + Volumes entfernen

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

PID_FILE=".demo-app.pid"

log() { printf "[stop-demo] %s\n" "$*"; }

# 1) App stoppen
if [[ -f "$PID_FILE" ]]; then
  PID=$(cat "$PID_FILE" 2>/dev/null || echo "")
  if [[ -n "$PID" ]] && ps -p "$PID" >/dev/null 2>&1; then
    log "Beende App (PID $PID)"
    kill "$PID" || true
    # Warte kurz und erzwinge ggf.
    for i in {1..10}; do
      if ps -p "$PID" >/dev/null 2>&1; then sleep 1; else break; fi
    done
    if ps -p "$PID" >/dev/null 2>&1; then
      log "Erzwinge Beenden (kill -9 $PID)"
      kill -9 "$PID" || true
    fi
  else
    log "Kein laufender App-Prozess gefunden."
  fi
  rm -f "$PID_FILE"
else
  log "PID-Datei nicht gefunden – App ist vermutlich nicht gestartet."
fi

# 2) DB stoppen oder entfernen
if [[ "${1:-}" == "--down" ]]; then
  log "Stoppe und entferne DB (docker compose down -v)"
  docker compose down -v || true
else
  log "Stoppe DB-Container (docker compose stop db)"
  docker compose stop db || true
fi

log "Fertig."

