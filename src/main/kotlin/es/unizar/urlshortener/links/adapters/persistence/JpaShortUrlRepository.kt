package es.unizar.urlshortener.links.adapters.persistence

import org.springframework.data.jpa.repository.JpaRepository
import java.time.Instant

interface JpaShortUrlRepository : JpaRepository<ShortUrlEntity, String> {
    fun findByExpiresAtLessThanEqual(cutoff: Instant): List<ShortUrlEntity>
}
