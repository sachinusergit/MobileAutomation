#!/usr/bin/env bash
#
# Generates or serves the Allure report using the fully qualified plugin goal.
#
# Usage:
#   ./scripts/allure-report.sh          # generate target/site/allure-maven-plugin
#   ./scripts/allure-report.sh --serve  # generate and serve on a local port

set -euo pipefail

SERVE=false
if [[ "${1:-}" == "--serve" ]]; then
  SERVE=true
fi

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$PROJECT_ROOT"

if [[ ! -x "./mvnw" ]]; then
  echo "Maven wrapper not found. Run 'mvn wrapper:wrapper' once." >&2
  exit 1
fi

if [[ "$SERVE" == "true" ]]; then
  GOAL="io.qameta.allure:allure-maven:2.15.0:serve"
else
  GOAL="io.qameta.allure:allure-maven:2.15.0:report"
fi

exec ./mvnw -B "$GOAL"
