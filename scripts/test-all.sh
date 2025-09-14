#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
cd "$REPO_ROOT"

echo "[test-all] Running full build + unit tests (no ITs)…"

start_ts=$(date +%s)

# Run Maven; capture exit code to always print a report
set +e
mvn -q -U -DskipITs verify
MVN_EXIT=$?
set -e

end_ts=$(date +%s)
duration=$(( end_ts - start_ts ))

# Aggregate Surefire results across all modules
total_tests=0
total_failures=0
total_errors=0
total_skipped=0

xml_reports="$(find . -type f -path "*/target/surefire-reports/*.xml" 2>/dev/null | sort)"

if [[ -n "$xml_reports" ]]; then
  while IFS= read -r xml; do
    # Portable awk: parse attributes explicitly and default to 0
    read -r t f e s < <(awk '
      /<testsuite/ {
        for (i=1; i<=NF; i++) {
          if ($i ~ /^tests=/)    { split($i,a,/"/); T+=a[2] }
          else if ($i ~ /^failures=/){ split($i,a,/"/); F+=a[2] }
          else if ($i ~ /^errors=/)  { split($i,a,/"/); E+=a[2] }
          else if ($i ~ /^skipped=/) { split($i,a,/"/); S+=a[2] }
        }
      }
      END { printf "%d %d %d %d", (T?T:0), (F?F:0), (E?E:0), (S?S:0) }
    ' "$xml" || true)
    t=${t:-0}; f=${f:-0}; e=${e:-0}; s=${s:-0}
    total_tests=$(( total_tests + t ))
    total_failures=$(( total_failures + f ))
    total_errors=$(( total_errors + e ))
    total_skipped=$(( total_skipped + s ))
  done <<< "$xml_reports"
fi

# Collect failing testcases (classname.method)
failing_list=""
if [[ -n "$xml_reports" ]]; then
  while IFS= read -r xml; do
    while IFS= read -r tc && [[ -n "$tc" ]]; do
      failing_list+="$tc
"
    done < <(awk -v RS='<|>' '
      $1 ~ /^testcase/ {
        classname=""; name="";
        n=split($0,a,/\s+/);
        for(i=1;i<=n;i++){
          if(a[i] ~ /^classname=/){ split(a[i],b,/"/); classname=b[2]; }
          if(a[i] ~ /^name=/){ split(a[i],c,/"/); name=c[2]; }
        }
        last=classname "." name;
      }
      $1 ~ /^(failure|error)$/ { if(last!=""){ print last; last="" } }
    ' "$xml" || true)
  done <<< "$xml_reports"
fi

echo ""
echo "================ Test Report ================"
printf "Result: %s\n" "$([[ $MVN_EXIT -eq 0 ]] && echo SUCCESS || echo FAIL)"
printf "Duration: %ss\n" "$duration"
printf "Tests: %d, Failures: %d, Errors: %d, Skipped: %d\n" "$total_tests" "$total_failures" "$total_errors" "$total_skipped"

if (( total_failures + total_errors > 0 )); then
  echo ""
  echo "Failing tests:"
  if [[ -n "$failing_list" ]]; then
    printf " - %s\n" $(printf "%s" "$failing_list" | sort -u)
  else
    echo " (no detailed list found)"
  fi
fi
echo "============================================"

exit "$MVN_EXIT"
