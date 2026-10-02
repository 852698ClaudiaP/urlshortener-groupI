package es.unizar.urlshortener.analytics

import es.unizar.urlshortener.analytics.application.PruneProcessedClickEvents
import es.unizar.urlshortener.analytics.application.UpdateLinkStats
import es.unizar.urlshortener.clicks.ClickLoggedEvent
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import java.time.Duration
import java.util.UUID

@SpringBootTest
class ProcessedClickEventPruneTests {
    @Autowired
    lateinit var updateLinkStats: UpdateLinkStats

    @Autowired
    lateinit var pruneProcessedClickEvents: PruneProcessedClickEvents

    @Autowired
    lateinit var jdbc: JdbcTemplate

    @Test
    fun `ids younger than the retention are kept`() {
        val event = click()
        pruneProcessedClickEvents.pruneOlderThan(Duration.ofDays(1))
        assertEquals(1, processedRows(event.eventId))
    }

    /** A negative retention puts the cutoff in the future, so every id counts as old. */
    @Test
    fun `ids older than the retention are removed`() {
        val event = click()
        pruneProcessedClickEvents.pruneOlderThan(Duration.ofSeconds(-1))
        assertEquals(0, processedRows(event.eventId))
    }

    private fun click() = ClickLoggedEvent(hash = "prune-${UUID.randomUUID()}").also(updateLinkStats::onClick)

    private fun processedRows(eventId: UUID): Int =
        jdbc.queryForObject("select count(*) from processed_click_event where event_id = ?", Int::class.java, eventId) ?: 0
}
