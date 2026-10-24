package es.unizar.urlshortener.clicks.application

import es.unizar.urlshortener.clicks.ClickLoggedEvent
import es.unizar.urlshortener.clicks.LiveClickEvent
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class RecordClickService(
    private val store: ClickStore,
    private val events: ApplicationEventPublisher,
) : RecordClick {
    @Transactional
    override fun record(event: ClickLoggedEvent) {
        if (store.existsByEventId(event.eventId)) return
        store.save(event.toClick())
        events.publishEvent(
            LiveClickEvent(
                hash = event.hash,
                occurredAt = event.occurredAt,
                eventId = event.eventId,
            ),
        )
    }
}

fun ClickLoggedEvent.toClick() = Click(hash = hash, occurredAt = occurredAt, eventId = eventId)
