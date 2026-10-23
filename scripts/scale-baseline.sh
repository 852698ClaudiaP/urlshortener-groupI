#!/usr/bin/env bash
# Replica-failover smoke against the product LB (same compose project).
# Usage: ./scripts/scale-baseline.sh [BASE_URL]
set -euo pipefail
BASE="${1:-http://localhost:8080}"

echo "== health via LB =="
curl -fsS "$BASE/actuator/health" | head -c 400
echo

echo "== create =="
RESP=$(curl -fsS -X POST "$BASE/api/link" -d "url=https://example.com/e1")
echo "$RESP"
HASH=$(python3 -c "import json,sys; print(json.load(sys.stdin)['hash'])" <<<"$RESP")

echo "== redirect (expect 307) =="
CODE=$(curl -s -o /dev/null -w "%{http_code}" "$BASE/$HASH")
test "$CODE" = "307"

echo "== kill one replica tip =="
echo "docker compose stop app2   # then repeat create/redirect — replica-failover evidence"

echo "OK hash=$HASH"
