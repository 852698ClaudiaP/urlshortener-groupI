package es.unizar.urlshortener

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.context.annotation.Bean
import org.testcontainers.postgresql.PostgreSQLContainer

@TestConfiguration(proxyBeanMethods = false)
class PostgresContainer {
    @Bean
    @ServiceConnection
    fun postgres() = PostgreSQLContainer("postgres:16-alpine")
}
