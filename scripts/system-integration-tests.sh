#!/usr/bin/env bash

set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:8080}"

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

jsonValue() {
  local path="$1"
  printf '%s' "$LAST_BODY" | jq -r "$path"
}

seller_payload='{
  "username": "bash-seller",
  "email": "bash-seller@example.com",
  "verified": true,
  "address": {
    "street": "100 Test",
    "city": "Montreal",
    "zipCode": "H1H1H1",
    "country": "Canada"
  }
}'

bidder_payload='{
  "username": "bash-bidder",
  "email": "bash-bidder@example.com",
  "verified": true,
  "address": {
    "street": "200 Test",
    "city": "Montreal",
    "zipCode": "H2H2H2",
    "country": "Canada"
  }
}'

assertCurl 201 POST /api/v1/users "$seller_payload"
seller_id="$(jsonValue '.userId')"
assertCurl 200 GET "/api/v1/users/$seller_id"
assertEqual "bash-seller@example.com" "$(jsonValue '.email')" "created seller can be fetched"

assertCurl 201 POST /api/v1/users "$bidder_payload"
bidder_id="$(jsonValue '.userId')"

listing_payload="$(jq -n \
  --arg sellerId "$seller_id" \
  '{sellerId:$sellerId,title:"Bash Listing",description:"Created by integration script",category:"Testing",condition:"GOOD"}')"

assertCurl 201 POST /api/v1/listings "$listing_payload"
listing_id="$(jsonValue '.listingId')"
assertCurl 200 GET "/api/v1/listings/$listing_id"
assertEqual "Bash Listing" "$(jsonValue '.title')" "created listing can be fetched"
assertCurl 200 POST "/api/v1/listings/$listing_id/publish"
assertEqual "true" "$(jsonValue '.published')" "listing is published before auction scheduling"

auction_payload="$(jq -n \
  --arg listingId "$listing_id" \
  --arg sellerId "$seller_id" \
  '{listingId:$listingId,sellerId:$sellerId,startTime:"2026-06-01T10:00:00",endTime:"2026-06-08T10:00:00",startingPrice:100.00,currency:"CAD"}')"

assertCurl 201 POST /api/v1/auctions "$auction_payload"
auction_id="$(jsonValue '.auctionId')"
assertCurl 200 GET "/api/v1/auctions/$auction_id"
assertEqual "$listing_id" "$(jsonValue '.listingId')" "created auction can be fetched as aggregate"
assertCurl 200 POST "/api/v1/auctions/$auction_id/activate"
assertEqual "ACTIVE" "$(jsonValue '.status')" "auction activates"

bid_payload="$(jq -n \
  --arg bidderId "$bidder_id" \
  '{bidderId:$bidderId,bidAmount:125.00,currency:"CAD"}')"

assertCurl 201 POST "/api/v1/auctions/$auction_id/bids" "$bid_payload"
assertCurl 200 GET "/api/v1/auctions/$auction_id/bids"
assertEqual "$bidder_id" "$(jsonValue '.[0].bidderId')" "auction bid is listed"

invoice_payload="$(jq -n \
  --arg auctionId "$auction_id" \
  --arg buyerId "$bidder_id" \
  --arg sellerId "$seller_id" \
  '{auctionId:$auctionId,buyerId:$buyerId,sellerId:$sellerId,dueDate:"2026-06-15T10:00:00",finalSaleAmount:125.00,currency:"CAD",method:"CREDIT_CARD"}')"

assertCurl 201 POST /api/v1/invoices "$invoice_payload"
invoice_id="$(jsonValue '.invoiceId')"
assertCurl 200 GET "/api/v1/invoices/$invoice_id"
assertEqual "$auction_id" "$(jsonValue '.auctionId')" "created invoice can be fetched"

assertCurl 404 GET /api/v1/auctions/missing-auction
assertEqual "Auction not found: missing-auction" "$(jsonValue '.message')" "negative auction lookup returns domain error"

printf '\nSystem integration script completed: %s passed, %s failed\n' "$PASS_COUNT" "$FAIL_COUNT"
