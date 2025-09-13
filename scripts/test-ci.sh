#!/usr/bin/env bash
set -euo pipefail
echo "[test-ci] Clean package then verify (no ITs)"
mvn -B -DskipTests clean package
mvn -B -U -DskipITs verify

