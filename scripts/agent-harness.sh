#!/usr/bin/env sh
set -eu

cd "$(dirname "$0")/.."

echo "== Backend tests =="
./gradlew :backend:test :ai-service:test

echo "== Frontend tests =="
(
  cd frontend
  npm test
)

echo "== Docker Compose config =="
docker compose config --quiet

echo "Agent harness checks passed."
