#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:8080}"

echo "[check-metrics] Health: $BASE_URL/actuator/health"
curl -fsS "$BASE_URL/actuator/health" | sed 's/^/[health] /' || { echo "[check-metrics] health failed"; exit 1; }

echo "[check-metrics] Prometheus: $BASE_URL/actuator/prometheus (first lines)"
curl -fsS "$BASE_URL/actuator/prometheus" | head -n 20 | sed 's/^/[prom]   /' || { echo "[check-metrics] prometheus failed"; exit 2; }

echo "[check-metrics] OK"

