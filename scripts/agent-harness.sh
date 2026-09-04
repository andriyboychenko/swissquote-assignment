#!/usr/bin/env sh
set -eu

cd "$(dirname "$0")/.."

echo "== Backend tests =="
./gradlew :backend:test

echo "== Frontend tests =="
(
  cd frontend
  npm test
)

echo "== Docker Compose config =="
docker compose --env-file gradle.properties config --quiet

echo "Agent harness checks passed."
