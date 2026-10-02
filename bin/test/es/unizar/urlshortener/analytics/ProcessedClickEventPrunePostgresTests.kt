package es.unizar.urlshortener.analytics

import es.unizar.urlshortener.PostgresContainer
import org.springframework.context.annotation.Import
import org.testcontainers.junit.jupiter.Testcontainers

/** Runs every [ProcessedClickEventPruneTests] case on Postgres. */
@Testcontainers(disabledWithoutDocker = true)
@Import(PostgresContainer::class)
class ProcessedClickEventPrunePostgresTests : ProcessedClickEventPruneTests()
