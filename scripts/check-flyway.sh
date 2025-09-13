#!/usr/bin/env bash
set -euo pipefail
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
cd "$REPO_ROOT"

DB_URL="${DB_URL:-jdbc:postgresql://localhost:5432/metering}"
DB_USER="${DB_USER:-metering}"
DB_PASS="${DB_PASS:-metering}"

echo "[check-flyway] flyway:info against $DB_URL"
mvn -q -pl app \
  -Dflyway.url="$DB_URL" \
  -Dflyway.user="$DB_USER" \
  -Dflyway.password="$DB_PASS" \
  flyway:info

