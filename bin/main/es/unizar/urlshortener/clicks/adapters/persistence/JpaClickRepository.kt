package es.unizar.urlshortener.clicks.adapters.persistence

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface JpaClickRepository : JpaRepository<ClickEntity, Long> {
    fun existsByEventId(eventId: UUID): Boolean
}
