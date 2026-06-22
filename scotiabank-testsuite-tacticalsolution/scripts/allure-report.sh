#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
RESULTS="$ROOT/target/allure-results"

if [ ! -d "$RESULTS" ] || [ -z "$(find "$RESULTS" -name '*-result.json' -print -quit 2>/dev/null)" ]; then
  echo "No hay resultados en $RESULTS"
  echo "Ejecuta primero: mvn test   (o corre los tests desde IntelliJ en la raíz del módulo)"
  exit 1
fi

COUNT=$(find "$RESULTS" -name '*-result.json' | wc -l | tr -d ' ')
echo "Resultados Allure: $COUNT escenario(s) en $RESULTS"
echo ""

cd "$ROOT"
if command -v allure >/dev/null 2>&1; then
  allure serve "$RESULTS"
else
  mvn allure:serve
fi
