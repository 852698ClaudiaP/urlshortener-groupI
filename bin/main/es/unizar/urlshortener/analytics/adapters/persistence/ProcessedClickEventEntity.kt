package es.unizar.urlshortener.analytics.adapters.persistence

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "processed_click_event")
class ProcessedClickEventEntity(
    @Id
    var eventId: UUID,
    var processedAt: Instant = Instant.now(),
)
