package es.unizar.urlshortener.clicks.application

import es.unizar.urlshortener.clicks.ClickLoggedEvent
import org.jmolecules.architecture.hexagonal.PrimaryPort
import org.jmolecules.architecture.hexagonal.SecondaryPort
import java.time.Instant
import java.util.UUID

data class Click(
    val hash: String,
    val occurredAt: Instant,
    val eventId: UUID,
)

@SecondaryPort
interface ClickStore {
    fun save(click: Click)

    fun existsByEventId(eventId: UUID): Boolean
}

@PrimaryPort
fun interface RecordClick {
    fun record(event: ClickLoggedEvent)
}
