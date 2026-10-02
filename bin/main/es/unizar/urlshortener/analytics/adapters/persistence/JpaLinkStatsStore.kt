package es.unizar.urlshortener.analytics.adapters.persistence

import es.unizar.urlshortener.analytics.application.LinkStats
import es.unizar.urlshortener.analytics.application.LinkStatsStore
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.UUID

@Component
class JpaLinkStatsStore(
    private val repository: JpaLinkStatsRepository,
    private val processed: JpaProcessedClickEventRepository,
) : LinkStatsStore {
    override fun findByHash(hash: String): LinkStats? = repository.findById(hash).map { it.toDomain() }.orElse(null)

    override fun insertIfAbsent(hash: String) {
        repository.insertIfAbsent(hash)
    }

    override fun incrementClicks(
        hash: String,
        eventId: UUID,
    ) {
        if (processed.existsById(eventId)) return
        processed.insert(eventId, Instant.now())
        repository.upsertIncrement(hash)
    }

    override fun deleteProcessedClicksBefore(cutoff: Instant): Int = processed.deleteProcessedBefore(cutoff)
}
