package es.unizar.urlshortener.analytics

import es.unizar.urlshortener.analytics.application.GetLinkStats
import es.unizar.urlshortener.analytics.application.UpdateLinkStats
import es.unizar.urlshortener.clicks.ClickLoggedEvent
import es.unizar.urlshortener.links.ShortUrlCreatedEvent
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.util.UUID
import java.util.concurrent.CountDownLatch
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@SpringBootTest
class LinkStatsConcurrencyTests {
    @Autowired
    lateinit var updateLinkStats: UpdateLinkStats

    @Autowired
    lateinit var getLinkStats: GetLinkStats

    @Test
    fun `concurrent first clicks on a missing row are all counted`() {
        val hash = "race-${UUID.randomUUID()}"
        runConcurrently(CLICKS) { updateLinkStats.onClick(ClickLoggedEvent(hash = hash)) }
            .forEach { it.getOrThrow() }
        assertEquals(CLICKS.toLong(), getLinkStats.findByHash(hash)?.totalClicks)
    }

    @Test
    fun `created event racing with clicks does not reset the counter`() {
        val hash = "race-${UUID.randomUUID()}"
        runConcurrently(CLICKS + 1) { index ->
            if (index == 0) {
                updateLinkStats.onCreated(ShortUrlCreatedEvent(hash = hash, target = "https://example.com"))
            } else {
                updateLinkStats.onClick(ClickLoggedEvent(hash = hash))
            }
        }.forEach { it.getOrThrow() }
        assertEquals(CLICKS.toLong(), getLinkStats.findByHash(hash)?.totalClicks)
    }

    @Test
    fun `a redelivered click is counted once`() {
        val event = ClickLoggedEvent(hash = "redelivery-${UUID.randomUUID()}")
        repeat(REDELIVERIES) { updateLinkStats.onClick(event) }
        assertEquals(1L, getLinkStats.findByHash(event.hash)?.totalClicks)
    }

    /** Copies racing past the processed-id check fail on its primary key and roll back; one is counted. */
    @Test
    fun `concurrent copies of one click are counted once`() {
        val event = ClickLoggedEvent(hash = "redelivery-${UUID.randomUUID()}")
        val results = runConcurrently(CLICKS) { updateLinkStats.onClick(event) }
        assert(results.any { it.isSuccess }) { "no copy succeeded: ${results.mapNotNull { it.exceptionOrNull() }}" }
        assertEquals(1L, getLinkStats.findByHash(event.hash)?.totalClicks)
    }

    private fun runConcurrently(
        tasks: Int,
        action: (Int) -> Unit,
    ): List<Result<Unit>> {
        val pool = Executors.newFixedThreadPool(tasks)
        val start = CountDownLatch(1)
        val futures =
            (0 until tasks).map { index ->
                pool.submit {
                    start.await()
                    action(index)
                }
            }
        start.countDown()
        return futures
            .map { future ->
                runCatching { future.get(TIMEOUT_SECONDS, TimeUnit.SECONDS) }
                    .map { }
                    .recoverCatching { throw (it as? ExecutionException)?.cause ?: it }
            }.also { pool.shutdown() }
    }

    private companion object {
        const val CLICKS = 16
        const val REDELIVERIES = 3
        const val TIMEOUT_SECONDS = 20L
    }
}
