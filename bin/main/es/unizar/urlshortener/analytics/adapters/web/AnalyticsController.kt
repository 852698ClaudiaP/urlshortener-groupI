package es.unizar.urlshortener.analytics.adapters.web

import es.unizar.urlshortener.analytics.application.GetLinkStats
import es.unizar.urlshortener.analytics.application.LinkStats
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.notFound
import org.springframework.http.ResponseEntity.ok
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

data class LinkStatsResponse(
    val hash: String,
    val totalClicks: Long,
)

@RestController
class AnalyticsController(
    private val getLinkStats: GetLinkStats,
) {
    @GetMapping("/api/stats/{hash}")
    fun stats(
        @PathVariable hash: String,
    ): ResponseEntity<LinkStatsResponse> {
        val stats = getLinkStats.findByHash(hash) ?: return notFound().build()
        return ok(stats.toResponse())
    }
}

private fun LinkStats.toResponse() = LinkStatsResponse(hash = hash, totalClicks = totalClicks)
