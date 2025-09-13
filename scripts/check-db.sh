#!/usr/bin/env bash
set -euo pipefail

echo "[check-db] Checking Docker Compose Postgres health…"
if ! command -v docker >/dev/null 2>&1; then
  echo "[check-db] ERROR: docker not found" >&2; exit 1
fi

CID=$(docker compose ps -q db || true)
if [[ -z "${CID:-}" ]]; then
  echo "[check-db] DB container not running. Start with: docker compose up -d db"; exit 2
fi

status=$(docker inspect -f '{{.State.Health.Status}}' "$CID" 2>/dev/null || echo "unknown")
echo "[check-db] Container: $CID  Health: $status"
if [[ "$status" != "healthy" ]]; then exit 3; fi

