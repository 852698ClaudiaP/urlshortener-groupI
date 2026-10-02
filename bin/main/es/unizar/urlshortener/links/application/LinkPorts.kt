package es.unizar.urlshortener.links.application

import es.unizar.urlshortener.links.domain.ShortUrl
import org.jmolecules.architecture.hexagonal.PrimaryPort
import org.jmolecules.architecture.hexagonal.SecondaryPort
import java.util.Optional

@SecondaryPort
interface ShortUrlStore {
    fun insert(shortUrl: ShortUrl): ShortUrl

    fun findByHash(hash: String): Optional<ShortUrl>
}

@SecondaryPort
fun interface UrlValidator {
    fun isValid(url: String): Boolean
}

@SecondaryPort
fun interface ShortCodeSource {
    fun next(): String
}

data class CreatedShortUrl(
    val hash: String,
    val target: String,
)

data class Redirection(
    val target: String,
)

class InvalidUrlException(
    url: String,
) : RuntimeException("[$url] is not a supported URL")

class LinkNotFoundException(
    hash: String,
) : RuntimeException("[$hash] is not known")

@PrimaryPort
fun interface CreateShortUrl {
    fun create(url: String): CreatedShortUrl
}

@PrimaryPort
fun interface RedirectShortUrl {
    fun redirect(hash: String): Redirection
}
