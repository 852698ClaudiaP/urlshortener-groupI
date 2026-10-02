package es.unizar.urlshortener.analytics.application

import es.unizar.urlshortener.clicks.ClickLoggedEvent
import es.unizar.urlshortener.links.ShortUrlCreatedEvent
import org.jmolecules.architecture.hexagonal.PrimaryPort
import org.jmolecules.architecture.hexagonal.SecondaryPort
import java.time.Duration
import java.time.Instant
import java.util.UUID

/** Per-hash click totals (one row per short URL). Not the permanent click log. */
data class LinkStats(
    val hash: String,
    val totalClicks: Long,
)

@SecondaryPort
interface LinkStatsStore {
    fun findByHash(hash: String): LinkStats?

    fun insertIfAbsent(hash: String)

    fun incrementClicks(
        hash: String,
        eventId: UUID,
    )

    fun deleteProcessedClicksBefore(cutoff: Instant): Int
}

@PrimaryPort
interface UpdateLinkStats {
    fun onCreated(event: ShortUrlCreatedEvent)

    fun onClick(event: ClickLoggedEvent)
}

@PrimaryPort
fun interface PruneProcessedClickEvents {
    /** Forgets processed click ids older than [retention]; returns how many were removed. */
    fun pruneOlderThan(retention: Duration): Int
}

@PrimaryPort
fun interface GetLinkStats {
    fun findByHash(hash: String): LinkStats?
}
