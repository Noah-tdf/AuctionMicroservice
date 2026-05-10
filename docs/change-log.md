# Milestone Change Log

## Starting point

- Source project started as a single-module Maven monolith in `src/`.
- Existing bounded-context candidates already present in the lab code: `usermanagement`, `listingmanagement`, `paymentprocessing`.

## Migration decisions

- Split the monolith into a Gradle multi-project workspace:
  - `api-gateway`
  - `auction-service`
  - `user-service`
  - `listing-service`
  - `invoice-service`
- Kept the monolith source in place as historical reference and migrated milestone code into the new modules.
- Removed HATEOAS from the low-level services and implemented hypermedia at the API gateway level only.
- Removed cross-service repository lookups from `listing-service` and `invoice-service` because Milestone 1 says not to implement cross-microservice aggregates yet.

## Manual code changes

- Added independent Spring Boot application entry points for all four modules.
- Added separate Flyway schemas and sample seed data for each low-level service.
- Added one subdomain-specific exception per low-level service:
  - `AuctionHasBidsException`
  - `DuplicateEmailException`
  - `ListingAlreadyPublishedException`
  - `InvoiceAlreadyPaidException`
- Reworked controllers to return plain JSON in low-level services.
- Added repository integration tests and controller integration tests with `WebTestClient`.
- Added JaCoCo reporting in the shared Gradle configuration.
- Replaced the original 3-container compose file with an 11-container microservices landscape:
  - API gateway
  - 4 low-level services
  - 4 databases
  - phpMyAdmin
  - pgAdmin

## Remaining validation

- Added a domain client layer to the API gateway so controllers delegate downstream calls instead of using `WebClient` directly.
- Added centralized HTTP client error translation and gateway-wide exception handling for downstream `4xx/5xx` and service-unavailable scenarios.
- Verified the exact peer-grading command `./gradlew clean build` succeeds locally after the gateway refactor.

## Milestone 2 updates

- Refactored the module packages to the teacher-style layers: `presentationlayer`, `businesslogiclayer`, `dataccesslayer`, `domain`, `utilities`, and `domainclientlayer`.
- Completed `auction-service` as the aggregator/orchestrator microservice.
- Switched the aggregator database from MySQL/JPA/Flyway to MongoDB/Spring Data MongoDB to match the Milestone 2 rubric wording.
- Added aggregator domain clients for listing, user, and invoice orchestration.
- Implemented the auction aggregate invariant:
  - seller must own the listing
  - listing must be published
  - seller must be verified
  - seller cannot bid on their own auction
  - closing a sold auction creates an invoice for the winning bid
- Updated the API gateway to expose all low-level service endpoints and aggregator endpoints through port `8080`.
- Added HATEOAS-style `_links` at the API gateway.
- Added and verified `scripts/system-integration-tests.sh`.
- Updated the DDD and C4 diagrams for Milestone 2.
- Kept database GUI port exposure in `docker-compose.gui.yml` so the main `docker-compose.yml` remains compliant with the API-gateway-only public port rule.
- Split database GUI tools into four containers: Auction Mongo Express, User phpMyAdmin, pgAdmin, and Invoice Mongo Express.
