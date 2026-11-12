#!/usr/bin/env bash
#
# Starts a local Appium server (Appium 2+).
#
# Usage:
#   ./scripts/start-appium.sh [address] [port] [log-level]

set -euo pipefail

ADDRESS="${1:-127.0.0.1}"
PORT="${2:-4723}"
LOG_LEVEL="${3:-info}"

if ! command -v appium >/dev/null 2>&1; then
  echo "Appium is not installed. Run: npm install -g appium; appium driver install uiautomator2; appium driver install xcuitest" >&2
  exit 1
fi

echo "Starting Appium on ${ADDRESS}:${PORT}"
exec appium --address "$ADDRESS" --port "$PORT" --log-level "$LOG_LEVEL"
