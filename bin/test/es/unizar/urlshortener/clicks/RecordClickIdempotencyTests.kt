package es.unizar.urlshortener.clicks

import es.unizar.urlshortener.clicks.application.RecordClick
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import java.util.UUID

@SpringBootTest
class RecordClickIdempotencyTests {
    @Autowired
    lateinit var recordClick: RecordClick

    @Autowired
    lateinit var jdbc: JdbcTemplate

    @Test
    fun `a redelivered click is logged once`() {
        val event = ClickLoggedEvent(hash = "redelivery-${UUID.randomUUID()}")
        repeat(REDELIVERIES) { recordClick.record(event) }
        assertEquals(1, rowsFor(event.hash))
    }

    @Test
    fun `distinct clicks on one hash are all logged`() {
        val hash = "clicks-${UUID.randomUUID()}"
        repeat(REDELIVERIES) { recordClick.record(ClickLoggedEvent(hash = hash)) }
        assertEquals(REDELIVERIES, rowsFor(hash))
    }

    private fun rowsFor(hash: String): Int = jdbc.queryForObject("select count(*) from click where hash = ?", Int::class.java, hash) ?: 0

    private companion object {
        const val REDELIVERIES = 3
    }
}
