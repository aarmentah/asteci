#!/usr/bin/env bash
set -euo pipefail

PASSPORT_BASE_URL="${PASSPORT_BASE_URL:?Define PASSPORT_BASE_URL}"
PASSPORT_TOKEN_PATH="${PASSPORT_TOKEN_PATH:-/48cf7cec-2dfe-4695-a3b1-eb423fc6418c}"

URL="${PASSPORT_BASE_URL%/}${PASSPORT_TOKEN_PATH}"
echo "GET $URL"
echo ""

curl -s --location --globoff "$URL" | (command -v jq >/dev/null && jq . || cat)
