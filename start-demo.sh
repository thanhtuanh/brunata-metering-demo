#!/usr/bin/env bash
set -euo pipefail

# Start Brunata Metering Demo: DB + App
# - Startet Postgres via Docker Compose und wartet auf "healthy"
# - Führt ein Flyway-Repair aus (hilft bei Checksummen-Mismatch in Dev)
# - Baut alle Module und startet die App im Hintergrund

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
cd "$SCRIPT_DIR"

LOG_FILE=".demo-app.log"
PID_FILE=".demo-app.pid"

# Optional: .env laden (falls vorhanden) und Variablen exportieren
if [[ -f "$REPO_ROOT/.env" ]]; then
  # shellcheck disable=SC1090
  set -a; source "$REPO_ROOT/.env"; set +a
fi

# DB-Parameter aus ENV (mit Defaults)
DB_URL="${DB_URL:-jdbc:postgresql://localhost:5432/metering}"
DB_USER="${DB_USER:-metering}"
DB_PASS="${DB_PASSWORD:-metering}"

log() { printf "[start-demo] %s\n" "$*"; }

port_in_use() { # $1 = port
  lsof -iTCP:"$1" -sTCP:LISTEN -n -P >/dev/null 2>&1
}

if command -v java >/dev/null 2>&1; then java -version 2>&1 | sed 's/^/[java] /'; else log "WARN: java nicht gefunden"; fi
if command -v mvn  >/dev/null 2>&1; then mvn -v      2>&1 | sed 's/^/[mvn]  /'; else log "WARN: mvn nicht gefunden"; fi
if ! command -v docker >/dev/null 2>&1; then log "ERROR: docker nicht gefunden"; exit 1; fi

# 1) DB starten
log "Starte Postgres (docker compose up -d db)"
docker compose up -d db

# 2) Auf Health warten
CID=$(docker compose ps -q db)
if [[ -z "${CID:-}" ]]; then log "ERROR: Kein DB-Container gefunden"; exit 1; fi

log "Warte auf DB-Health (bis 120s) …"
for i in {1..120}; do
  status=$(docker inspect -f '{{.State.Health.Status}}' "$CID" 2>/dev/null || true)
  if [[ "$status" == "healthy" ]]; then
    log "DB ist healthy."
    break
  fi
  sleep 1
  if [[ $i -eq 120 ]]; then
    log "WARN: Healthcheck nicht healthy nach 120s (Status: $status). Fahre fort."
  fi
done

# 3) Flyway-Repair (nur Dev; behebt geänderte Migrationen wie V2__billing.sql)
log "Führe Flyway-Repair aus (Dev-Helfer gegen Checksum-Mismatch)"
mvn -q -pl app \
  -Dflyway.url="$DB_URL" \
  -Dflyway.user="$DB_USER" \
  -Dflyway.password="$DB_PASS" \
  flyway:repair || log "Hinweis: flyway:repair fehlgeschlagen oder nicht nötig"

# 4) Build aller Module (ohne Tests für Geschwindigkeit)
log "Baue alle Module (clean install, Tests komplett überspringen)"
mvn -q -U -Dmaven.test.skip=true clean install

# 5) App starten (Hintergrund) und PID speichern
if [[ -f "$PID_FILE" ]] && ps -p "$(cat "$PID_FILE" 2>/dev/null || echo 0)" >/dev/null 2>&1; then
  log "App scheint bereits zu laufen (PID $(cat "$PID_FILE")), breche ab."
  exit 0
fi

# Port ermitteln bzw. freigeben
BASE_PORT="${PORT:-8080}"
CHOSEN_PORT="$BASE_PORT"
if port_in_use "$CHOSEN_PORT"; then
  for try in 8081 8082 8083 8084 8085; do
    if ! port_in_use "$try"; then CHOSEN_PORT="$try"; break; fi
  done
  if [[ "$CHOSEN_PORT" != "$BASE_PORT" ]]; then
    log "Port $BASE_PORT belegt. Weiche auf Port $CHOSEN_PORT aus."
  else
    log "ERROR: Kein freier Port (8080-8085) gefunden."
    exit 1
  fi
fi

log "Starte App im Hintergrund auf Port $CHOSEN_PORT (Logs: $LOG_FILE) …"
nohup mvn -q -pl app -Dspring-boot.run.arguments=--server.port="$CHOSEN_PORT" spring-boot:run > "$LOG_FILE" 2>&1 & echo $! > "$PID_FILE"

# Warten und Health prüfen
for i in {1..30}; do
  if ! ps -p "$(cat "$PID_FILE" 2>/dev/null || echo 0)" >/dev/null 2>&1; then
    log "ERROR: App-Prozess vorzeitig beendet. Prüfe $LOG_FILE."
    rm -f "$PID_FILE"
    exit 1
  fi
  if command -v curl >/dev/null 2>&1; then
    if curl -fsS "http://localhost:${CHOSEN_PORT}/actuator/health" >/dev/null 2>&1; then
      break
    fi
  fi
  sleep 1
done

log "App gestartet. PID $(cat "$PID_FILE")."
log "Endpoints: http://localhost:${CHOSEN_PORT}/actuator/health  |  http://localhost:${CHOSEN_PORT}/swagger-ui/index.html"
log "Landing:   http://localhost:${CHOSEN_PORT}/"
log "Logs verfolgen: tail -f $LOG_FILE"

# Hinweis: Demo-Daten werden nun per Flyway-Migration (V5__demo_seed.sql) angelegt.
# Kein zusätzlicher API-Seed-Aufruf mehr nötig.
