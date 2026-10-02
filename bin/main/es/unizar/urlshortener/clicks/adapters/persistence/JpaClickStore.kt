package es.unizar.urlshortener.clicks.adapters.persistence

import es.unizar.urlshortener.clicks.application.Click
import es.unizar.urlshortener.clicks.application.ClickStore
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class JpaClickStore(
    private val repository: JpaClickRepository,
) : ClickStore {
    override fun save(click: Click) {
        repository.save(click.toEntity())
    }

    override fun existsByEventId(eventId: UUID): Boolean = repository.existsByEventId(eventId)
}
