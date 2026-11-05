# ADR 0006: URL Accessibility Validation

- **Status:** Accepted
- **Date:** 2026-11-05
- **Deciders:** Alejandro Vives Ruiz / Feature [`0009`](../features/0009-URL-Accessibility-Validation.md)

## Context

Feature `0009` requires that the application validates whether a target URL is accessible and returns an acceptable HTTP status code before storing it and generating a short code.

This introduces several architectural and operational challenges:
1. **Creation path latency**: An external network round-trip during link creation can cause threads to hang if the target host is slow or unresponsive. A strict timeout is mandatory to maintain Level 2 scalability.
2. **Server idiosyncrasies (HEAD vs. GET)**: While `HEAD` is the most efficient method (avoids downloading the body), some web servers or CDNs return `405 Method Not Allowed` or drop `HEAD` requests. A resilient fallback to `GET` is required.
3. **Redirect loops and broken redirects**: Treating redirects (3xx) blindly as success can lead to shortening broken endpoints. The validator must follow redirects up to a bounded limit.
4. **Standardised error reporting**: The course baseline requires RFC 9457 Problem Details for all error responses instead of custom JSON structures.

## Decision

We will implement URL accessibility validation inside the `links` module with the following architecture:

1. **Secondary Port & Adapter**:
   - Introduce an `UrlAccessibilityValidator` contract in `links.application` (or integrate it into the `UrlValidator` port).
   - Implement `HttpUrlAccessibilityValidator` in `links.adapters.validation` using `java.net.http.HttpClient` (Java 25 LTS built-in, avoiding extra heavy dependencies) configured with:
     - Strict connect timeout (2000 ms).
     - Strict request timeout (3000 ms).
     - Redirect policy: `HttpClient.Redirect.NORMAL` (follows up to a maximum number of hops).
2. **Validation Strategy**:
   - First, execute fast local syntax validation via Apache Commons (`CommonsUrlValidator`).
   - Next, issue a lightweight `HEAD` request.
   - If the remote server responds with `405 Method Not Allowed` or `501 Not Implemented`, automatically retry with a `GET` request.
   - Consider acceptable status codes: `2xx` (Success) and `3xx` (Redirection).
   - Reject with `UrlNotReachableException` on `4xx`, `5xx`, DNS failures, or timeouts.
3. **RFC 9457 Problem Details**:
   - Register exception handlers in `LinkController` (or `@ControllerAdvice`) that return Spring's native `ProblemDetail` with status `400 Bad Request`, `title = "URL Not Reachable"`, and informative `detail` and custom properties (`targetUrl`, `cause`).

## Consequences

### Positive
- Prevents dead or unreachable URLs from entering the database.
- Adheres strictly to RFC 9457 Problem Details (Technologies slice baseline).
- Bounded timeouts protect the web server's worker threads under load.
- Resilient against servers rejecting `HEAD` requests.

### Negative
- Link creation latency increases by the duration of the outbound HTTP request.
- External network availability may influence creation of external URLs in integration environments (handled via mocked validators in unit/integration test suites).

## Alternatives considered

| Option | Why rejected |
| --- | --- |
| Synchronous `HttpURLConnection` without timeouts | Can hang indefinitely if remote server stalls, violating Level 2 scalability. |
| Spring WebClient (Project Reactor) | The seed project is built on standard Spring MVC (`spring-boot-starter-webmvc`). Pulling in WebFlux solely for one validation check adds unnecessary dependency overhead. |
| Only check syntax (`CommonsUrlValidator`) | Does not satisfy Feature 0009 requirements (does not check real target accessibility). |

## Evidence (defence)

- [ ] Validator implementation in `es.unizar.urlshortener.links.adapters.validation`
- [ ] RFC 9457 error handler in `es.unizar.urlshortener.links.adapters.web.LinkController`
- [ ] Unit & integration tests in `es.unizar.urlshortener.links.UrlAccessibilityValidationTests`
- [ ] Automated verification: `./gradlew check` (ktlint + tests + JaCoCo coverage >= 60%)
