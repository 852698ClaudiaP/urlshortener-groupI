package es.unizar.urlshortener.links.application

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class DeleteExpiredShortUrlsService(
    private val store: ShortUrlStore,
) : DeleteExpiredShortUrls {
    @Transactional
    override fun delete(cutoff: Instant) {
        val expired = store.findExpiredBefore(cutoff)
        expired.forEach { shortUrl -> store.deleteByHash(shortUrl.hash) }
    }
}
