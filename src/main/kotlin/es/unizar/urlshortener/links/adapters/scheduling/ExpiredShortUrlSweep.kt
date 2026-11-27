package es.unizar.urlshortener.links.adapters.scheduling

import es.unizar.urlshortener.links.application.DeleteExpiredShortUrls
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class ExpiredShortUrlSweep(
    private val deleteExpiredShortUrls: DeleteExpiredShortUrls,
) {
    @Scheduled(cron = "0 0 0 * * *", zone = "Europe/Madrid")
    fun removeUrls() {
        deleteExpiredShortUrls.delete(Instant.now())
    }
}
