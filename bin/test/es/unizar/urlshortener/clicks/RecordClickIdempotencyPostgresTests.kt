package es.unizar.urlshortener.clicks

import es.unizar.urlshortener.PostgresContainer
import org.springframework.context.annotation.Import
import org.testcontainers.junit.jupiter.Testcontainers

/** Runs every [RecordClickIdempotencyTests] case on Postgres. */
@Testcontainers(disabledWithoutDocker = true)
@Import(PostgresContainer::class)
class RecordClickIdempotencyPostgresTests : RecordClickIdempotencyTests()
