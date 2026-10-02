package es.unizar.urlshortener.links.domain

import java.time.Instant

data class ShortUrl(
    val hash: String,
    val target: String,
    val createdAt: Instant = Instant.now(),
)
