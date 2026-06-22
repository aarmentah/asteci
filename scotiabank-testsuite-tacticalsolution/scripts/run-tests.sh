#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
export JAVA_HOME="${JAVA_HOME:-$(/usr/libexec/java_home -v 17)}"

BASE_URI="${API_BASE_URI:-http://localhost:8080}"
ACCESS_TOKEN="${API_ACCESS_TOKEN:-}"
HEALTH_PATH="${API_HEALTH_PATH:-/api/v1/customers/curp-validation}"

echo "Java: $($JAVA_HOME/bin/java -version 2>&1 | head -1)"
echo "API:  $BASE_URI"

if [ -z "$ACCESS_TOKEN" ]; then
  echo ""
  echo "Advertencia: API_ACCESS_TOKEN no está definido."
  echo "Exporta un token válido: API_ACCESS_TOKEN=<token> ./scripts/run-tests.sh"
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

mvn clean test "${MVN_ARGS[@]}" "$@"
