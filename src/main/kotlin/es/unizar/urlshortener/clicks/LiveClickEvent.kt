package es.unizar.urlshortener.clicks

import java.time.Instant
import java.util.UUID

/**
 * Domain event published when a click has been successfully recorded,
 * intended for real-time consumers (such as the streaming module).
 */
data class LiveClickEvent(
    val hash: String,
    val occurredAt: Instant = Instant.now(),
    val eventId: UUID = UUID.randomUUID(),
)
