package es.unizar.urlshortener.links.adapters.persistence

import es.unizar.urlshortener.links.application.ShortUrlStore
import es.unizar.urlshortener.links.domain.ShortUrl
import org.springframework.stereotype.Component
import java.util.Optional

@Component
class JpaShortUrlStore(
    private val repository: JpaShortUrlRepository,
) : ShortUrlStore {
    override fun insert(shortUrl: ShortUrl): ShortUrl = repository.save(shortUrl.toEntity()).toDomain()

    override fun findByHash(hash: String): Optional<ShortUrl> = repository.findById(hash).map { it.toDomain() }
}
