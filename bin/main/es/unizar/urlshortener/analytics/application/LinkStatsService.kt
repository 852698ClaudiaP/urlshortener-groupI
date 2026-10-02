package es.unizar.urlshortener.analytics.application

import es.unizar.urlshortener.clicks.ClickLoggedEvent
import es.unizar.urlshortener.links.ShortUrlCreatedEvent
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.time.Instant

private val log = KotlinLogging.logger {}

@Service
class LinkStatsService(
    private val store: LinkStatsStore,
) : UpdateLinkStats,
    PruneProcessedClickEvents,
    GetLinkStats {
    @Transactional
    override fun onCreated(event: ShortUrlCreatedEvent) {
        store.insertIfAbsent(event.hash)
        log.info { "stats ready for ${event.hash}" }
    }

    @Transactional
    override fun onClick(event: ClickLoggedEvent) {
        store.incrementClicks(event.hash, event.eventId)
    }

    @Transactional
    override fun pruneOlderThan(retention: Duration): Int =
        store.deleteProcessedClicksBefore(Instant.now().minus(retention)).also { removed ->
            log.info { "pruned $removed processed click ids older than $retention" }
        }

    @Transactional(readOnly = true)
    override fun findByHash(hash: String): LinkStats? = store.findByHash(hash)
}
