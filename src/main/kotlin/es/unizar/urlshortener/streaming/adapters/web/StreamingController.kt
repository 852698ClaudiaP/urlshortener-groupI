package es.unizar.urlshortener.streaming.adapters.web

import es.unizar.urlshortener.streaming.application.RegisterLiveClickListener
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.io.IOException

@RestController
@RequestMapping("/api/streaming")
class StreamingController(
    private val registerListener: RegisterLiveClickListener,
) {
    private val logger = LoggerFactory.getLogger(StreamingController::class.java)

    @GetMapping("/clicks", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun streamAllClicks(): SseEmitter = createEmitter(null)

    @GetMapping("/clicks/{hash}", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun streamClicksForHash(
        @PathVariable hash: String,
    ): SseEmitter = createEmitter(hash)

    private fun createEmitter(hash: String?): SseEmitter {
        val emitter = SseEmitter(DEFAULT_TIMEOUT_MS)
        val registration =
            registerListener.register(hash) { clickedHash, timestamp ->
                try {
                    emitter.send(
                        SseEmitter
                            .event()
                            .name("click")
                            .data(mapOf("hash" to clickedHash, "timestamp" to timestamp)),
                    )
                } catch (e: IOException) {
                    logger.debug("Client disconnected: {}", e.message)
                    emitter.complete()
                } catch (e: IllegalStateException) {
                    logger.debug("Emitter already closed: {}", e.message)
                }
            }

        emitter.onCompletion { registration.close() }
        emitter.onTimeout {
            emitter.complete()
            registration.close()
        }
        emitter.onError { registration.close() }
        sendInitialEvent(emitter, hash, registration)
        return emitter
    }

    private fun sendInitialEvent(
        emitter: SseEmitter,
        hash: String?,
        registration: AutoCloseable,
    ) {
        try {
            emitter.send(
                SseEmitter
                    .event()
                    .name("connected")
                    .data(mapOf("status" to "connected", "filter" to (hash ?: "*"))),
            )
        } catch (e: IOException) {
            logger.debug("Failed to send initial SSE event: {}", e.message)
            registration.close()
        }
    }

    private companion object {
        const val DEFAULT_TIMEOUT_MS = 60_000L
    }
}
