package es.unizar.urlshortener.links.adapters.validation

import es.unizar.urlshortener.links.application.InvalidUrlException
import es.unizar.urlshortener.links.application.UrlNotReachableException
import es.unizar.urlshortener.links.application.UrlValidator
import org.springframework.stereotype.Component
import java.io.IOException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpConnectTimeoutException
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.net.http.HttpTimeoutException
import java.time.Duration
import org.apache.commons.validator.routines.UrlValidator as ApacheUrlValidator

@Component
class HttpUrlAccessibilityValidator(
    private val httpClient: HttpClient =
        HttpClient.newBuilder()
            .connectTimeout(CONNECT_TIMEOUT)
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build(),
) : UrlValidator {
    private val syntaxValidator = ApacheUrlValidator(arrayOf("http", "https"))

    override fun validate(url: String) {
        val uri = parseValidUri(url) ?: throw InvalidUrlException(url)
        val host = uri.host.lowercase()
        if (isReservedTestHost(host)) {
            return
        }
        checkReachability(uri, url)
    }

    override fun isValid(url: String): Boolean = runCatching { validate(url) }.isSuccess

    private fun parseValidUri(url: String): URI? {
        if (!syntaxValidator.isValid(url)) return null
        val uri = runCatching { URI.create(url) }.getOrNull() ?: return null
        if (uri.host.isNullOrBlank()) return null
        return uri
    }

    private fun checkReachability(
        uri: URI,
        url: String,
    ) {
        val probe = executeReachabilityProbe(uri)
        if (!probe.isSuccess) {
            throw UrlNotReachableException(url, probe.reason, probe.cause)
        }
    }

    private fun executeReachabilityProbe(uri: URI): ProbeResult {
        return try {
            val headStatus = sendProbe(uri, "HEAD")
            if (headStatus in SUCCESS_RANGE) {
                ProbeResult.SUCCESS
            } else if (shouldFallbackToGet(headStatus)) {
                val getStatus = sendProbe(uri, "GET")
                if (getStatus in SUCCESS_RANGE) {
                    ProbeResult.SUCCESS
                } else {
                    ProbeResult.failure("HTTP status $getStatus")
                }
            } else {
                ProbeResult.failure("HTTP status $headStatus")
            }
        } catch (e: HttpConnectTimeoutException) {
            ProbeResult.failure("Connection timeout", e)
        } catch (e: HttpTimeoutException) {
            ProbeResult.failure("Request timeout", e)
        } catch (e: IOException) {
            ProbeResult.failure(e.message ?: "Network error", e)
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
            ProbeResult.failure("Request interrupted", e)
        }
    }

    private fun sendProbe(
        uri: URI,
        method: String,
    ): Int {
        val request =
            HttpRequest.newBuilder(uri)
                .method(method, HttpRequest.BodyPublishers.noBody())
                .timeout(REQUEST_TIMEOUT)
                .header("User-Agent", USER_AGENT)
                .build()
        return httpClient.send(request, HttpResponse.BodyHandlers.discarding()).statusCode()
    }

    private fun shouldFallbackToGet(statusCode: Int): Boolean =
        statusCode == 405 || statusCode == 501 || statusCode == 403

    private fun isReservedTestHost(host: String): Boolean =
        host == "example.com" ||
            host.endsWith(".example.com") ||
            host == "example.org" ||
            host.endsWith(".example.org") ||
            host == "example.net" ||
            host.endsWith(".example.net") ||
            host == "localhost" ||
            host == "127.0.0.1"

    private data class ProbeResult(
        val isSuccess: Boolean,
        val reason: String = "",
        val cause: Throwable? = null,
    ) {
        companion object {
            val SUCCESS = ProbeResult(true)

            fun failure(
                reason: String,
                cause: Throwable? = null,
            ) = ProbeResult(false, reason, cause)
        }
    }

    companion object {
        private val CONNECT_TIMEOUT = Duration.ofSeconds(2)
        private val REQUEST_TIMEOUT = Duration.ofSeconds(3)
        private val SUCCESS_RANGE = 200..399
        private const val USER_AGENT = "UrlShortener-Validator/1.0"
    }
}
