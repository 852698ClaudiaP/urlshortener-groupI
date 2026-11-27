# ADR 0007: CSV Upload

- **Status:** Proposed 
- **Date:** 2026-11-27
- **Deciders:** Claudia Pavón Calcerrada / Feature [0004](../features/0004-CSV-upload.md)

## Context

Feature `0004` requires batch URL shortening through CSV file uploads. Users must be able to upload a CSV containing multiple URLs and receive a new CSV containing the original columns plus shortUrl and status.

Each row must be processed independently so that an invalid or unreachable URL does not prevent the remaining rows from being processed.

The implementation must handle RFC 4180 CSV edge cases. It must also distinguish between request-level errors (e.g. malformed CSV or missing required columns) and row-level errors (e.g. invalid or unreachable URLs).

A CSV parsing library is needed to ensure correct CSV handling and to simplify reading and generating CSV files.

## Decision

The feature will be implemented in the `links` module using the following structure:
- `links.application.LinkCSVService`: responsible for parsing the uploaded CSV, processing individual rows and generating the output CSV.
- `links.adapters.web.CsvUploadController`: exposes a multipart/form-data endpoint (POST /api/links/upload) and returns the generated CSV as a downloadable file.
- The application service will depend on the existing `CreateShortUrl` primary port to create shortened URLs, avoiding duplication of business logic.

Apache Commons CSV has been chosen as the CSV processing library. It will be used as follows:
- CSVParser to read CSV records using the RFC 4180 format.
- CSVPrinter to generate the resulting CSV.

Each row of the CSV will be processed independently. The existing `ShortUrlCreatedEvent` will continue to be published by the URL-shortening application service for every successfully processed row. A new event called `CSVUploadedEvent` will be created at the end of the process.

## Consequences

### Positive consequences

- Apache Commons CSV provides reliable RFC 4180 parsing and CSV generation.
- Each row is processed independently, so one failure does not abort the entire batch.
- Reusing `CreateShortUrl` ensures that CSV uploads follow the same validation, code generation, persistence and event publication rules as individual URL creation.
- The new `CsvUploadCompletedEvent` allows other modules to react to completed CSV uploads without coupling them directly to the CSV service.

### Negative consequences

- Apache Commons CSV introduces an additional project dependency.
- Large CSV files may consume significant memory if the entire input and output are held in memory.

### Testing and CI requirements

- `./gradlew` test must pass.
- Unit tests must verify CSV parsing, successful shortening, row-level error handling and continuation after failures.
- Tests must cover RFC 4180 edge cases, including quoted commas, escaped quotes and embedded newlines.
- Existing link creation and redirection tests must continue passing.

## Alternatives considered

| Option | Why rejected |
| --- | --- |
| … | … |
| … | … |

## Evidence (defence)

- [ ] Code / PR link
- [ ] Test or command that proves the decision (`./gradlew test`, k6, `docker compose …`)
- [ ] Diagram or Modulith doc snippet (`build/spring-modulith-docs/`) if structure changed
