#!/bin/bash

# Test script for CSMS Authorization API

BASE_URL="http://localhost:8080/api/v1/authorization"
STATION_UUID="25aac66b-6051-478a-95e2-6d3aa343b025"

echo "=== Testing CSMS Authorization API ==="
echo ""

# Check if jq is available, otherwise use cat
if command -v jq &> /dev/null; then
    JSON_FORMATTER="jq ."
else
    JSON_FORMATTER="cat"
fi

echo "1. Testing Accepted (known and allowed identifier)..."
curl -s -X POST "$BASE_URL" \
  -H "Content-Type: application/json" \
  -d "{
    \"stationUuid\": \"$STATION_UUID\",
    \"driverIdentifier\": {\"id\": \"id12345678901234567890\"}
  }" | $JSON_FORMATTER
echo ""
echo ""

echo "2. Testing Unknown identifier..."
curl -s -X POST "$BASE_URL" \
  -H "Content-Type: application/json" \
  -d "{
    \"stationUuid\": \"$STATION_UUID\",
    \"driverIdentifier\": {\"id\": \"unknown12345678901234567890\"}
  }" | $JSON_FORMATTER
echo ""
echo ""

echo "3. Testing Invalid identifier (too short)..."
curl -s -X POST "$BASE_URL" \
  -H "Content-Type: application/json" \
  -d "{
    \"stationUuid\": \"$STATION_UUID\",
    \"driverIdentifier\": {\"id\": \"short\"}
  }" | $JSON_FORMATTER
echo ""
echo ""

echo "4. Testing Invalid identifier (too long)..."
LONG_ID=$(printf 'a%.0s' {1..81})
curl -s -X POST "$BASE_URL" \
  -H "Content-Type: application/json" \
  -d "{
    \"stationUuid\": \"$STATION_UUID\",
    \"driverIdentifier\": {\"id\": \"$LONG_ID\"}
  }" | $JSON_FORMATTER
echo ""
echo ""

echo "5. Testing Rejected (known but not allowed)..."
curl -s -X POST "$BASE_URL" \
  -H "Content-Type: application/json" \
  -d "{
    \"stationUuid\": \"$STATION_UUID\",
    \"driverIdentifier\": {\"id\": \"rejected12345678901234567890\"}
  }" | $JSON_FORMATTER
echo ""
echo ""

echo "=== Tests Complete ==="
