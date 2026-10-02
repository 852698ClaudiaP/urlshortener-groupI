package es.unizar.urlshortener.clicks.adapters.events

import es.unizar.urlshortener.clicks.ClickLoggedEvent
import es.unizar.urlshortener.clicks.application.RecordClick
import org.springframework.modulith.events.ApplicationModuleListener
import org.springframework.stereotype.Component

@Component
class ClickRecorder(
    private val recordClick: RecordClick,
) {
    @ApplicationModuleListener
    fun on(event: ClickLoggedEvent) {
        recordClick.record(event)
    }
}
