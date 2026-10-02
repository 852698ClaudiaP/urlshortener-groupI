package es.unizar.urlshortener.analytics.adapters.persistence

import es.unizar.urlshortener.analytics.application.LinkStats
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "link_stats")
class LinkStatsEntity(
    @Id
    var hash: String,
    var totalClicks: Long = 0,
)

fun LinkStatsEntity.toDomain() = LinkStats(hash = hash, totalClicks = totalClicks)
