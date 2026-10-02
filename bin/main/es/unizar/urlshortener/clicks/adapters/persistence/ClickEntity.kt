package es.unizar.urlshortener.clicks.adapters.persistence

import es.unizar.urlshortener.clicks.application.Click
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType.IDENTITY
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "click")
class ClickEntity(
    @Id
    @GeneratedValue(strategy = IDENTITY)
    var id: Long? = null,
    var hash: String,
    var occurredAt: Instant = Instant.now(),
    @Column(unique = true)
    var eventId: UUID? = null,
)

fun Click.toEntity() = ClickEntity(hash = hash, occurredAt = occurredAt, eventId = eventId)
