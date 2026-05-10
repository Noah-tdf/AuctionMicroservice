#!/usr/bin/env bash

set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:8080}"
RUN_ID="${RUN_ID:-$(date +%s)}"

if ! command -v curl >/dev/null 2>&1; then
  printf 'Missing required command: curl\n'
  exit 1
fi

if command -v python3 >/dev/null 2>&1; then
  PYTHON_BIN="python3"
elif command -v python >/dev/null 2>&1; then
  PYTHON_BIN="python"
else
  printf 'Missing required command: python or python3\n'
  exit 1
fi

PASS_COUNT=0
FAIL_COUNT=0
LAST_BODY=""
LAST_STATUS=""

assertCurl() {
  local expected_status="$1"
  local method="$2"
  local path="$3"
  local payload="${4:-}"

  local response
  if [[ -n "$payload" ]]; then
    response="$(curl -sS -w '\n%{http_code}' -X "$method" "$BASE_URL$path" \
      -H "Content-Type: application/json" \
      -d "$payload")"
  else
    response="$(curl -sS -w '\n%{http_code}' -X "$method" "$BASE_URL$path")"
  fi

  LAST_STATUS="$(printf '%s' "$response" | tail -n 1)"
  LAST_BODY="$(printf '%s' "$response" | sed '$d')"

  if [[ "$LAST_STATUS" == "$expected_status" ]]; then
    PASS_COUNT=$((PASS_COUNT + 1))
    printf 'PASS %s %s -> %s\n' "$method" "$path" "$LAST_STATUS"
  else
    FAIL_COUNT=$((FAIL_COUNT + 1))
    printf 'FAIL %s %s expected %s got %s\n%s\n' "$method" "$path" "$expected_status" "$LAST_STATUS" "$LAST_BODY"
    return 1
  fi
}

assertEqual() {
  local expected="$1"
  local actual="$2"
  local label="$3"

  if [[ "$expected" == "$actual" ]]; then
    PASS_COUNT=$((PASS_COUNT + 1))
    printf 'PASS %s\n' "$label"
  else
    FAIL_COUNT=$((FAIL_COUNT + 1))
    printf 'FAIL %s expected [%s] got [%s]\n' "$label" "$expected" "$actual"
    return 1
  fi
}

assertJsonCollectionContains() {
  local array_path="$1"
  local field="$2"
  local expected="$3"
  local label="$4"

  if printf '%s' "$LAST_BODY" | JSON_ARRAY_PATH="$array_path" JSON_FIELD="$field" JSON_EXPECTED="$expected" "$PYTHON_BIN" -c 'import json, os, sys
data = json.load(sys.stdin)
for part in os.environ["JSON_ARRAY_PATH"].strip(".").split("."):
    if part:
        data = data.get(part, [])
field = os.environ["JSON_FIELD"]
expected = os.environ["JSON_EXPECTED"]
for item in data or []:
    if str(item.get(field)) == expected:
        sys.exit(0)
sys.exit(1)' >/dev/null; then
    PASS_COUNT=$((PASS_COUNT + 1))
    printf 'PASS %s\n' "$label"
  else
    FAIL_COUNT=$((FAIL_COUNT + 1))
    printf 'FAIL %s\n%s\n' "$label" "$LAST_BODY"
    return 1
  fi
}

jsonValue() {
  local path="$1"
  printf '%s' "$LAST_BODY" | JSON_PATH="$path" "$PYTHON_BIN" -c 'import json, os, sys
data = json.load(sys.stdin)
for part in os.environ["JSON_PATH"].strip(".").split("."):
    if not part:
        continue
    data = data[int(part)] if isinstance(data, list) else data.get(part)
if isinstance(data, bool):
    print(str(data).lower())
elif data is None:
    print("")
elif isinstance(data, float) and data.is_integer():
    print(int(data))
else:
    print(data)'
}

seller_payload=$(cat <<JSON
{
  "username": "bash-seller-$RUN_ID",
  "email": "bash-seller-$RUN_ID@example.com",
  "isVerified": true,
  "address": {
    "street": "100 Test",
    "city": "Montreal",
    "zipCode": "H1H1H1",
    "country": "Canada"
  }
}
JSON
)

bidder_payload=$(cat <<JSON
{
  "username": "bash-bidder-$RUN_ID",
  "email": "bash-bidder-$RUN_ID@example.com",
  "isVerified": true,
  "address": {
    "street": "200 Test",
    "city": "Montreal",
    "zipCode": "H2H2H2",
    "country": "Canada"
  }
}
JSON
)

assertCurl 201 POST /api/v1/users "$seller_payload"
seller_id="$(jsonValue '.userId')"
assertCurl 200 GET /api/v1/users
assertJsonCollectionContains ".items" "userId" "$seller_id" "created seller appears in gateway user collection"
assertCurl 200 GET "/api/v1/users/$seller_id"
assertEqual "bash-seller-$RUN_ID@example.com" "$(jsonValue '.email')" "created seller can be fetched"

assertCurl 201 POST /api/v1/users "$bidder_payload"
bidder_id="$(jsonValue '.userId')"

listing_payload=$(cat <<JSON
{
  "sellerId": "$seller_id",
  "title": "Bash Listing $RUN_ID",
  "description": "Created by integration script",
  "category": "Testing",
  "condition": "GOOD"
}
JSON
)

assertCurl 201 POST /api/v1/listings "$listing_payload"
listing_id="$(jsonValue '.listingId')"
assertCurl 200 GET /api/v1/listings
assertJsonCollectionContains ".items" "listingId" "$listing_id" "created listing appears in gateway listing collection"
assertCurl 200 GET "/api/v1/listings/$listing_id"
assertEqual "Bash Listing $RUN_ID" "$(jsonValue '.title')" "created listing can be fetched"
assertCurl 200 POST "/api/v1/listings/$listing_id/publish"
assertEqual "true" "$(jsonValue '.published')" "listing is published before auction scheduling"

auction_payload=$(cat <<JSON
{
  "listingId": "$listing_id",
  "sellerId": "$seller_id",
  "startTime": "2026-06-01T10:00:00",
  "endTime": "2026-06-08T10:00:00",
  "startingPrice": 100.00,
  "currency": "CAD"
}
JSON
)

assertCurl 201 POST /api/v1/auctions "$auction_payload"
auction_id="$(jsonValue '.auctionId')"
assertCurl 200 GET /api/v1/auctions
assertJsonCollectionContains ".items" "auctionId" "$auction_id" "created auction appears in gateway aggregate collection"
assertCurl 200 GET "/api/v1/auctions/$auction_id"
assertEqual "$listing_id" "$(jsonValue '.listingId')" "created auction can be fetched as aggregate"

auction_update_payload=$(cat <<JSON
{
  "startTime": "2026-06-02T10:00:00",
  "endTime": "2026-06-09T10:00:00",
  "startingPrice": 125.00,
  "currency": "CAD"
}
JSON
)
assertCurl 200 PUT "/api/v1/auctions/$auction_id" "$auction_update_payload"
assertEqual "125" "$(jsonValue '.startingPrice')" "scheduled auction can be updated"

invalid_auction_payload=$(cat <<JSON
{
  "listingId": "$listing_id",
  "sellerId": "$bidder_id",
  "startTime": "2026-06-01T10:00:00",
  "endTime": "2026-06-08T10:00:00",
  "startingPrice": 100.00,
  "currency": "CAD"
}
JSON
)
assertCurl 409 POST /api/v1/auctions "$invalid_auction_payload"
assertEqual "Auction seller must own listing: $listing_id" "$(jsonValue '.message')" "aggregate invariant rejects non-owner seller"

assertCurl 200 POST "/api/v1/auctions/$auction_id/activate"
assertEqual "ACTIVE" "$(jsonValue '.status')" "auction activates"

bid_payload=$(cat <<JSON
{
  "bidderId": "$bidder_id",
  "bidAmount": 150.00,
  "currency": "CAD"
}
JSON
)
assertCurl 201 POST "/api/v1/auctions/$auction_id/bids" "$bid_payload"
bid_id="$(jsonValue '.bidId')"
assertCurl 200 GET "/api/v1/auctions/$auction_id/bids"
assertJsonCollectionContains ".items" "bidderId" "$bidder_id" "auction bid is listed through HATEOAS collection"

bid_update_payload=$(cat <<JSON
{
  "bidAmount": 175.00,
  "currency": "CAD"
}
JSON
)
assertCurl 200 PUT "/api/v1/auctions/$auction_id/bids/$bid_id" "$bid_update_payload"
assertEqual "175" "$(jsonValue '.bidAmount')" "auction bid can be updated"

assertCurl 200 GET /api/v1/bids
assertJsonCollectionContains ".items" "bidId" "$bid_id" "global bid endpoint exposes the created bid"

assertCurl 200 POST "/api/v1/auctions/$auction_id/close"
assertEqual "SOLD" "$(jsonValue '.status')" "closing an auction with bids marks it sold"

invoice_payload=$(cat <<JSON
{
  "auctionId": "$auction_id",
  "buyerId": "$bidder_id",
  "sellerId": "$seller_id",
  "dueDate": "2026-06-15T10:00:00",
  "finalSaleAmount": 175.00,
  "currency": "CAD",
  "method": "CREDIT_CARD"
}
JSON
)
assertCurl 201 POST /api/v1/invoices "$invoice_payload"
invoice_id="$(jsonValue '.invoiceId')"
assertCurl 200 GET /api/v1/invoices
assertJsonCollectionContains ".items" "invoiceId" "$invoice_id" "created invoice appears in gateway invoice collection"
assertCurl 200 GET "/api/v1/invoices/$invoice_id"
assertEqual "$auction_id" "$(jsonValue '.auctionId')" "created invoice can be fetched"

delete_auction_payload=$(cat <<JSON
{
  "listingId": "$listing_id",
  "sellerId": "$seller_id",
  "startTime": "2026-06-03T10:00:00",
  "endTime": "2026-06-10T10:00:00",
  "startingPrice": 90.00,
  "currency": "CAD"
}
JSON
)
assertCurl 201 POST /api/v1/auctions "$delete_auction_payload"
deletable_auction_id="$(jsonValue '.auctionId')"
assertCurl 204 DELETE "/api/v1/auctions/$deletable_auction_id"

delete_bid_payload=$(cat <<JSON
{
  "listingId": "$listing_id",
  "sellerId": "$seller_id",
  "startTime": "2026-06-04T10:00:00",
  "endTime": "2026-06-11T10:00:00",
  "startingPrice": 80.00,
  "currency": "CAD"
}
JSON
)
assertCurl 201 POST /api/v1/auctions "$delete_bid_payload"
delete_bid_auction_id="$(jsonValue '.auctionId')"
assertCurl 200 POST "/api/v1/auctions/$delete_bid_auction_id/activate"
assertCurl 201 POST "/api/v1/auctions/$delete_bid_auction_id/bids" "$bid_payload"
deletable_bid_id="$(jsonValue '.bidId')"
assertCurl 204 DELETE "/api/v1/auctions/$delete_bid_auction_id/bids/$deletable_bid_id"

assertCurl 404 GET /api/v1/auctions/missing-auction
assertEqual "Auction not found: missing-auction" "$(jsonValue '.message')" "negative auction lookup returns domain error"

printf '\nSystem integration script completed: %s passed, %s failed\n' "$PASS_COUNT" "$FAIL_COUNT"
