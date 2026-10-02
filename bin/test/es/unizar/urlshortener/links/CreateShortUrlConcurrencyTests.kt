package es.unizar.urlshortener.links

import es.unizar.urlshortener.links.adapters.persistence.Base62
import es.unizar.urlshortener.links.application.CreateShortUrl
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.util.UUID
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@SpringBootTest
class CreateShortUrlConcurrencyTests {
    @Autowired
    lateinit var createShortUrl: CreateShortUrl

    @Test
    fun `each create mints a new code of at least the minimum length`() {
        val url = "https://example.com/${UUID.randomUUID()}"
        val first = createShortUrl.create(url).hash
        val second = createShortUrl.create(url).hash
        assertNotEquals(first, second)
        assertTrue(first.length >= Base62.MIN_CODE_LENGTH)
        assertTrue(second.length >= Base62.MIN_CODE_LENGTH)
    }

    @Test
    fun `concurrent creates of the same URL each get a distinct code`() {
        val url = "https://example.com/${UUID.randomUUID()}"
        val results = runConcurrently(CREATES) { createShortUrl.create(url).hash }
        assertEquals(CREATES, results.toSet().size)
        assertTrue(results.all { it.length >= Base62.MIN_CODE_LENGTH })
    }

    private fun <T> runConcurrently(
        tasks: Int,
        action: (Int) -> T,
    ): List<T> {
        val pool = Executors.newFixedThreadPool(tasks)
        val start = CountDownLatch(1)
        val futures =
            (0 until tasks).map { index ->
                pool.submit<T> {
                    start.await()
                    action(index)
                }
            }
        start.countDown()
        return futures.map { it.get(TIMEOUT_SECONDS, TimeUnit.SECONDS) }.also { pool.shutdown() }
    }

    private companion object {
        const val CREATES = 16
        const val TIMEOUT_SECONDS = 20L
    }
}
