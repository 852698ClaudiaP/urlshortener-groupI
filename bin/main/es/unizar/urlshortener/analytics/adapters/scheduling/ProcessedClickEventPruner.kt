package es.unizar.urlshortener.analytics.adapters.scheduling

import es.unizar.urlshortener.analytics.application.PruneProcessedClickEvents
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Duration

/**
 * Retention must exceed the time an incomplete publication can wait before being resubmitted;
 * otherwise a resubmitted click is counted again. Running on every replica is harmless.
 */
@Component
class ProcessedClickEventPruner(
    private val prune: PruneProcessedClickEvents,
    @param:Value("\${urlshortener.analytics.processed-click-retention}") private val retention: Duration,
) {
    @Scheduled(cron = "\${urlshortener.analytics.processed-click-prune-cron}")
    fun prune() {
        prune.pruneOlderThan(retention)
    }
}
