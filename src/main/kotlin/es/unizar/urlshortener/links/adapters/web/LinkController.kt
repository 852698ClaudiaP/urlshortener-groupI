package es.unizar.urlshortener.links.adapters.web

import es.unizar.urlshortener.links.application.CreateShortUrl
import es.unizar.urlshortener.links.application.InvalidUrlException
import es.unizar.urlshortener.links.application.LinkNotFoundException
import es.unizar.urlshortener.links.application.RedirectShortUrl
import es.unizar.urlshortener.links.application.ShortUrlExpiredException
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import java.net.URI
import java.time.Instant

data class ShortUrlResponse(
    val url: String,
    val hash: String,
    val expiresAt: Instant?,
)

@RestController
class LinkController(
    private val createShortUrl: CreateShortUrl,
    private val redirectShortUrl: RedirectShortUrl,
) {
    @PostMapping(
        "/api/link",
        consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE],
    )
    fun create(
        @RequestParam url: String,
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        expiresAt: Instant?,
    ): ResponseEntity<ShortUrlResponse> {
        val created = createShortUrl.create(url, expiresAt)
        val location =
            ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path("/{hash}")
                .buildAndExpand(created.hash)
                .toUri()
        return ResponseEntity.created(location).body(
            ShortUrlResponse(url = location.toString(), hash = created.hash, expiresAt = expiresAt),
        )
    }

    @GetMapping("/{hash}")
    fun redirect(
        @PathVariable hash: String,
    ): ResponseEntity<Void> {
        val redirection = redirectShortUrl.redirect(hash)
        val headers = HttpHeaders()
        headers.location = URI.create(redirection.target)
        return ResponseEntity(headers, HttpStatus.TEMPORARY_REDIRECT)
    }

    @ExceptionHandler(InvalidUrlException::class)
    fun invalidUrl(ex: InvalidUrlException): ResponseEntity<Map<String, String>> =
        ResponseEntity.badRequest().body(mapOf("error" to (ex.message ?: "invalid url")))

    @ExceptionHandler(LinkNotFoundException::class)
    fun notFound(ex: LinkNotFoundException): ResponseEntity<Map<String, String>> =
        ResponseEntity.status(HttpStatus.NOT_FOUND).body(mapOf("error" to (ex.message ?: "not found")))

    @ExceptionHandler(ShortUrlExpiredException::class)
    fun urlEpired(ex: ShortUrlExpiredException): ResponseEntity<Map<String, String>> =
        ResponseEntity.status(HttpStatus.GONE).body(mapOf("error" to (ex.message ?: "url expired")))
}
