#!/usr/bin/env bash
#
# Runs the mobile test suite using the Maven wrapper.
#
# Usage:
#   ./scripts/run-tests.sh [android|ios] [dev|qa|staging|prod|ci] [suite-file]

set -euo pipefail

PLATFORM="${1:-android}"
ENVIRONMENT="${2:-dev}"
SUITE="${3:-src/test/resources/suites/testng-smoke.xml}"

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$PROJECT_ROOT"

if [[ ! -x "./mvnw" ]]; then
  echo "Maven wrapper not found. Run 'mvn wrapper:wrapper' once." >&2
  exit 1
fi

echo "Running mobile tests | platform=${PLATFORM} environment=${ENVIRONMENT} suite=${SUITE}"

./mvnw -B test "-Denv=${ENVIRONMENT}" "-Dplatform=${PLATFORM}" "-DsuiteXmlFile=${SUITE}"
