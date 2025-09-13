#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:8080}"
# Optional Basic Auth: export BASIC_USER=demo BASIC_PASS=demo123
AUTH_OPT=""
if [[ -n "${BASIC_USER:-}" && -n "${BASIC_PASS:-}" ]]; then
  AUTH_OPT=(-u "${BASIC_USER}:${BASIC_PASS}")
fi

echo "[check-metrics] Health: $BASE_URL/actuator/health"
curl -fsS "${AUTH_OPT[@]}" "$BASE_URL/actuator/health" | sed 's/^/[health] /' || { echo "[check-metrics] health failed"; exit 1; }

echo "[check-metrics] Prometheus: $BASE_URL/actuator/prometheus (first lines)"
curl -fsS "${AUTH_OPT[@]}" "$BASE_URL/actuator/prometheus" | head -n 20 | sed 's/^/[prom]   /' || { echo "[check-metrics] prometheus failed"; exit 2; }

echo "[check-metrics] OK"
