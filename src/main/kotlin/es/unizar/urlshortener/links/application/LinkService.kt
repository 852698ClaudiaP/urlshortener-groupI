package es.unizar.urlshortener.links.application

import es.unizar.urlshortener.clicks.ClickLoggedEvent
import es.unizar.urlshortener.links.ShortUrlCreatedEvent
import es.unizar.urlshortener.links.domain.ShortUrl
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class LinkService(
    private val store: ShortUrlStore,
    private val validator: UrlValidator,
    private val codes: ShortCodeSource,
    private val events: ApplicationEventPublisher,
) : CreateShortUrl,
    RedirectShortUrl {
    @Transactional
    override fun create(
        url: String,
        expiresAt: Instant?,
    ): CreatedShortUrl {
        val trimmed = url.trim()
        require(trimmed.isNotEmpty()) { "url must not be blank" }
        if (!validator.isValid(trimmed)) {
            throw InvalidUrlException(trimmed)
        }
        val stored = store.insert(ShortUrl(hash = codes.next(), target = trimmed, expiresAt = expiresAt))
        events.publishEvent(ShortUrlCreatedEvent(hash = stored.hash, target = stored.target))
        return CreatedShortUrl(hash = stored.hash, target = stored.target)
    }

    @Transactional
    override fun redirect(hash: String): Redirection {
        val shortUrl = store.findByHash(hash).orElseThrow { LinkNotFoundException(hash) }
        if (shortUrl.expiresAt != null && !shortUrl.expiresAt.isAfter(Instant.now())) {
            throw ShortUrlExpiredException(hash)
        }
        events.publishEvent(ClickLoggedEvent(hash = hash, occurredAt = Instant.now()))
        return Redirection(target = shortUrl.target)
    }
}
