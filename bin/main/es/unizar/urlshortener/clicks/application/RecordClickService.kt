package es.unizar.urlshortener.clicks.application

import es.unizar.urlshortener.clicks.ClickLoggedEvent
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class RecordClickService(
    private val store: ClickStore,
) : RecordClick {
    @Transactional
    override fun record(event: ClickLoggedEvent) {
        if (store.existsByEventId(event.eventId)) return
        store.save(event.toClick())
    }
}

fun ClickLoggedEvent.toClick() = Click(hash = hash, occurredAt = occurredAt, eventId = eventId)
