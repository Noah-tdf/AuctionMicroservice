# Milestone 2 Compliance Notes

## Architecture

- `settings.gradle` includes the five required services:
  - `api-gateway`
  - `auction-service`
  - `user-service`
  - `listing-service`
  - `invoice-service`
- `auction-service` is the aggregator/orchestrator microservice. It owns the `Auction` aggregate, persists it in MongoDB, and orchestrates `listing-service`, `user-service`, and `invoice-service` through its `domainclientlayer`.
- Low-level microservices:
  - `user-service`
  - `listing-service`
  - `invoice-service`
- Layer names follow the teacher style:
  - `presentationlayer`
  - `presentationlayer/dto`
  - `datamappinglayer`
  - `businesslogiclayer`
  - `dataccesslayer`
  - `domain`
  - `utilities`
  - `domainclientlayer` where HTTP clients are needed
  - `domainclientlayer/dto` where downstream HTTP DTOs are needed
- Controllers delegate request/response conversion to mapper classes instead of carrying mapping code and DTO records inside controller files.

## Design Evidence

- DDD Domain Model source: `diagrams/ddd-domain-model.puml`
- DDD Domain Model PNG: `diagrams/ddd-domain-model.png`
- C4 Level 1 source: `diagrams/C4_Level1_context.puml`
- C4 Level 1 PNG: `diagrams/C4_Level1_context.png`
- C4 Level 2 source: `diagrams/C4_Level2_container.puml`
- C4 Level 2 PNG: `diagrams/C4_Level2_container.png`
- The DDD model now explicitly states the aggregate invariant for auction scheduling, bidding, and closing.

## Docker

- Main compose file: `docker-compose.yml`
- Only the API gateway publishes a host port in the main compose file: `8080:8080`.
- Backend services and databases stay inside Docker networking.
- The main compose file defines 13 containers: API gateway, four Spring Boot backend services, four databases, and four database GUI tools.
- The aggregator database is MongoDB, matching the Milestone 2 rubric's aggregator persistence wording.
- Browser database GUI ports are kept in the optional override file `docker-compose.gui.yml` for presentation use.

Strict grading run:

```bash
docker compose up -d
```

Presentation run with database GUI access:

```bash
docker compose -f docker-compose.yml -f docker-compose.gui.yml up -d
```

## Testing And Coverage

Verification command:

```bash
./gradlew clean build
```

Current line coverage from JaCoCo:

- `api-gateway`: 99.1%
- `auction-service`: 94.7%
- `invoice-service`: 96.4%
- `listing-service`: 95.3%
- `user-service`: 91.7%

The build is configured to fail if any service drops below 90% line coverage.

## System Integration Script

- Script: `scripts/system-integration-tests.sh`
- Runs all requests through the API gateway at `http://localhost:8080`.
- Covers GET and POST requests for users, listings, auctions, bids, and invoices.
- Covers the aggregator workflow, HATEOAS collection responses, a custom aggregate-invariant negative case, and a not-found negative case.

Run with Git Bash:

```bash
bash scripts/system-integration-tests.sh
```

Last verified result:

```text
System integration script completed: 46 passed, 0 failed
```

## Submission Items Still Manual

- Add the updated PNG diagrams separately to Moodle.
- Include screenshots of the five JaCoCo reports.
- Include a screenshot of `./gradlew clean build` showing PASS/FAIL/SKIP test output.
- Include screenshots of positive and negative controller integration exception tests.
- Fill in the Word document table with the test evidence counts.
