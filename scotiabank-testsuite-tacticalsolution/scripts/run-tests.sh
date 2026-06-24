#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
export JAVA_HOME="${JAVA_HOME:-$(/usr/libexec/java_home -v 17)}"

BASE_URI="${API_BASE_URI:-http://localhost:8080}"
ACCESS_TOKEN="${API_ACCESS_TOKEN:-}"
PASSPORT_BASE_URL="${PASSPORT_BASE_URL:-}"
PASSPORT_TOKEN_PATH="${PASSPORT_TOKEN_PATH:-/48cf7cec-2dfe-4695-a3b1-eb423fc6418c}"
HEALTH_PATH="${API_HEALTH_PATH:-/api/v1/customers/curp-validation}"

extract_passport_token() {
  local response="$1"

  if command -v jq >/dev/null 2>&1; then
    echo "$response" | jq -r '.access_token // .token // .data.access_token // .data.token // .'
    return
  fi

  if echo "$response" | grep -q '"access_token"'; then
    echo "$response" | sed -n 's/.*"access_token"[[:space:]]*:[[:space:]]*"\([^"]*\)".*/\1/p'
    return
  fi

  if echo "$response" | grep -q '"token"'; then
    echo "$response" | sed -n 's/.*"token"[[:space:]]*:[[:space:]]*"\([^"]*\)".*/\1/p'
    return
  fi

  echo "$response" | tr -d '\n\r" '
}

fetch_passport_token() {
  local url="${PASSPORT_BASE_URL%/}${PASSPORT_TOKEN_PATH}"
  local response
  response=$(curl -sf --location --globoff "$url")
  extract_passport_token "$response"
}

echo "Java: $($JAVA_HOME/bin/java -version 2>&1 | head -1)"
echo "API:  $BASE_URI"

if [ -z "$ACCESS_TOKEN" ] && [ -n "$PASSPORT_BASE_URL" ]; then
  echo "Passport: $PASSPORT_BASE_URL"
  ACCESS_TOKEN="$(fetch_passport_token)"
  echo "Token obtenido desde Passport."
fi

if [ -z "$ACCESS_TOKEN" ]; then
  echo ""
  echo "Advertencia: no hay token disponible."
  echo "Opciones:"
  echo "  API_ACCESS_TOKEN=<token> ./scripts/run-tests.sh"
  echo "  PASSPORT_BASE_URL=<passport-base-url> ./scripts/run-tests.sh"
fi

if ! curl -sf --connect-timeout 3 -o /dev/null -X POST "$BASE_URI$HEALTH_PATH" \
  -H 'Content-Type: application/json' \
  ${ACCESS_TOKEN:+-H "Authorization: Bearer $ACCESS_TOKEN"} \
  -H 'x-b3-traceid: 1' \
  -H 'x-b3-spanid: 1' \
  -H 'x-channel-id: 1' \
  -H 'x-originating-appl-code: 1' \
  -H 'x-country-code: 1' \
  -H 'x-user-context: 1' \
  -H 'x-api-version: 1' \
  -d '{"curp":"PEGC880313MDFRVR03","request_id":"CUAC01726763563935"}' 2>/dev/null; then
  echo ""
  echo "No hay respuesta en $BASE_URI (Connection refused o timeout)."
  echo "Levanta el servicio IB BaaS Customer Validation MX y vuelve a ejecutar."
  echo "Si usa otra URL: API_BASE_URI=http://host:puerto ./scripts/run-tests.sh"
  exit 1
fi

cd "$ROOT"
MVN_ARGS=(-Dapi.base.uri="$BASE_URI")
if [ -n "$ACCESS_TOKEN" ]; then
  MVN_ARGS+=(-Dapi.access.token="$ACCESS_TOKEN")
fi
if [ -n "$PASSPORT_BASE_URL" ]; then
  MVN_ARGS+=(-Dpassport.base.uri="$PASSPORT_BASE_URL" -Dpassport.token.path="$PASSPORT_TOKEN_PATH")
fi

mvn clean test "${MVN_ARGS[@]}" "$@"
