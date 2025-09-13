#!/usr/bin/env bash
set -euo pipefail
echo "[test-integration] Running AppIntegrationTest (Testcontainers required)"
mvn -B -pl app -Dtest=AppIntegrationTest test

