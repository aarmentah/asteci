#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
export JAVA_HOME="${JAVA_HOME:-$(/usr/libexec/java_home -v 17)}"

BASE_URI="${API_BASE_URI:-http://localhost:8081}"
HEALTH_PATH="${API_HEALTH_PATH:-/api/v1/authentication/login}"

echo "Java: $($JAVA_HOME/bin/java -version 2>&1 | head -1)"
echo "API:  $BASE_URI"

if ! curl -sf --connect-timeout 3 -o /dev/null -X POST "$BASE_URI$HEALTH_PATH" \
  -H 'Content-Type: application/json' -H 'country-id: MX' \
  -d '{"data":{"login":{"user_id":"ping@test.com"}}}' 2>/dev/null; then
  echo ""
  echo "No hay respuesta en $BASE_URI (Connection refused o timeout)."
  echo "Levanta tu servicio de login en el puerto 8081 y vuelve a ejecutar."
  echo "Si usa otra URL: API_BASE_URI=http://host:puerto ./scripts/run-tests.sh"
  exit 1
fi

cd "$ROOT"
mvn clean test -Dapi.base.uri="$BASE_URI" "$@"
