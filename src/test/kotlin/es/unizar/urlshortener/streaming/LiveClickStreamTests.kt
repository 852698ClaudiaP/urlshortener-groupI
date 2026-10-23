package es.unizar.urlshortener.streaming

import es.unizar.urlshortener.clicks.LiveClickEvent
import es.unizar.urlshortener.streaming.adapters.events.LiveClickListener
import es.unizar.urlshortener.streaming.adapters.web.StreamingController
import es.unizar.urlshortener.streaming.application.BroadcastLiveClick
import es.unizar.urlshortener.streaming.application.RegisterLiveClickListener
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.request
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.UUID
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@SpringBootTest
@AutoConfigureMockMvc
class LiveClickStreamTests {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var registerListener: RegisterLiveClickListener

    @Autowired
    lateinit var broadcastLiveClick: BroadcastLiveClick

    @Autowired
    lateinit var liveClickListener: LiveClickListener

    @Autowired
    lateinit var streamingController: StreamingController

    @Test
    fun `streaming endpoint returns text event stream and async started`() {
        mockMvc
            .perform(get("/api/streaming/clicks"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.TEXT_EVENT_STREAM_VALUE))
            .andExpect(request().asyncStarted())

        val testHash = "hash-${UUID.randomUUID()}"
        mockMvc
            .perform(get("/api/streaming/clicks/$testHash"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.TEXT_EVENT_STREAM_VALUE))
            .andExpect(request().asyncStarted())
    }

    @Test
    fun `global subscriber receives live click event`() {
        val testHash = "hash-${UUID.randomUUID()}"
        val receivedClicks = CopyOnWriteArrayList<String>()
        val latch = CountDownLatch(1)

        val registration =
            registerListener.register(null) { hash, _ ->
                if (hash == testHash) {
                    receivedClicks.add(hash)
                    latch.countDown()
                }
            }

        try {
            broadcastLiveClick.broadcast(testHash, System.currentTimeMillis())
            assertTrue(latch.await(3, TimeUnit.SECONDS), "Subscriber did not receive broadcast within timeout")
            assertEquals(listOf(testHash), receivedClicks)
        } finally {
            registration.close()
        }
    }

    @Test
    fun `hash-filtered subscriber only receives its own hash clicks`() {
        val targetHash = "target-${UUID.randomUUID()}"
        val otherHash = "other-${UUID.randomUUID()}"
        val receivedClicks = CopyOnWriteArrayList<String>()
        val latch = CountDownLatch(1)

        val registration =
            registerListener.register(targetHash) { hash, _ ->
                receivedClicks.add(hash)
                latch.countDown()
            }

        try {
            broadcastLiveClick.broadcast(otherHash, System.currentTimeMillis())
            broadcastLiveClick.broadcast(targetHash, System.currentTimeMillis())

            assertTrue(latch.await(3, TimeUnit.SECONDS), "Target subscriber did not receive its click")
            assertEquals(listOf(targetHash), receivedClicks)
        } finally {
            registration.close()
        }
    }

    @Test
    fun `live click listener handles domain event from clicks module`() {
        val testHash = "listener-${UUID.randomUUID()}"
        val receivedClicks = CopyOnWriteArrayList<String>()
        val latch = CountDownLatch(1)

        val registration =
            registerListener.register(testHash) { hash, _ ->
                receivedClicks.add(hash)
                latch.countDown()
            }

        try {
            val event = LiveClickEvent(hash = testHash)
            liveClickListener.on(event)

            assertTrue(latch.await(3, TimeUnit.SECONDS), "Listener did not trigger broadcast on domain event")
            assertEquals(listOf(testHash), receivedClicks)
        } finally {
            registration.close()
        }
    }

    @Test
    fun `controller createEmitter returns valid SseEmitter`() {
        val emitterAll = streamingController.streamAllClicks()
        assertNotNull(emitterAll)

        val emitterHash = streamingController.streamClicksForHash("test-hash")
        assertNotNull(emitterHash)
    }
}
