# Milestone 1 Change Log

## Starting point

- Source project started as a single-module Maven monolith in `src/`.
- Existing bounded-context candidates already present in the lab code: `usermanagement`, `listingmanagement`, `paymentprocessing`.

## Migration decisions

- Split the monolith into a Gradle multi-project workspace:
  - `api-gateway`
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
  - `DuplicateEmailException`
  - `ListingAlreadyPublishedException`
  - `InvoiceAlreadyPaidException`
- Reworked controllers to return plain JSON in low-level services.
- Added repository integration tests and controller integration tests with `WebTestClient`.
- Added JaCoCo reporting in the shared Gradle configuration.
- Replaced the original 3-container compose file with a 9-container microservices landscape:
  - API gateway
  - 3 low-level services
  - 3 databases
  - phpMyAdmin
  - pgAdmin

## Remaining validation

- `gradlew` is not yet present in this workspace and still needs to be generated or added before the exact peer-grading command `./gradlew clean build` can be executed.
- The new code should be verified with a full Gradle build and `docker compose up --build` once Gradle wrapper support is available locally.
