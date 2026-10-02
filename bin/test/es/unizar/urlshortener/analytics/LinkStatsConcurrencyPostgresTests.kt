package es.unizar.urlshortener.analytics

import es.unizar.urlshortener.PostgresContainer
import org.springframework.context.annotation.Import
import org.testcontainers.junit.jupiter.Testcontainers

@Testcontainers(disabledWithoutDocker = true)
@Import(PostgresContainer::class)
class LinkStatsConcurrencyPostgresTests : LinkStatsConcurrencyTests()
