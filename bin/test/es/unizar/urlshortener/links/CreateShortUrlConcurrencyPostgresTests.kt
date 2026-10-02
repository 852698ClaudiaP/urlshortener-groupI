package es.unizar.urlshortener.links

import es.unizar.urlshortener.PostgresContainer
import org.springframework.context.annotation.Import
import org.testcontainers.junit.jupiter.Testcontainers

@Testcontainers(disabledWithoutDocker = true)
@Import(PostgresContainer::class)
class CreateShortUrlConcurrencyPostgresTests : CreateShortUrlConcurrencyTests()
