package es.unizar.urlshortener.streaming.adapters.events

import es.unizar.urlshortener.clicks.LiveClickEvent
import es.unizar.urlshortener.streaming.application.BroadcastLiveClick
import org.springframework.modulith.events.ApplicationModuleListener
import org.springframework.stereotype.Component

@Component
class LiveClickListener(
    private val broadcastLiveClick: BroadcastLiveClick,
) {
    @ApplicationModuleListener
    fun on(event: LiveClickEvent) {
        broadcastLiveClick.broadcast(event.hash, event.occurredAt.toEpochMilli())
    }
}
