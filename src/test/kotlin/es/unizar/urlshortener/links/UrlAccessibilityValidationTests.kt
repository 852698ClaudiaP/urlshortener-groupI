package es.unizar.urlshortener.links

import es.unizar.urlshortener.links.adapters.validation.HttpUrlAccessibilityValidator
import es.unizar.urlshortener.links.application.InvalidUrlException
import es.unizar.urlshortener.links.application.UrlNotReachableException
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.io.IOException
import java.net.http.HttpClient
import java.net.http.HttpConnectTimeoutException
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.net.http.HttpTimeoutException

@SpringBootTest
@AutoConfigureMockMvc
class UrlAccessibilityValidationTests {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `AC1 - accessible URL with 2xx returns 201 Created and location header`() {
        val mockClient = mock(HttpClient::class.java)
        @Suppress("UNCHECKED_CAST")
        val mockResponse = mock(HttpResponse::class.java) as HttpResponse<Void>
        `when`(mockResponse.statusCode()).thenReturn(200)
        `when`(mockClient.send(any(HttpRequest::class.java), any<HttpResponse.BodyHandler<Void>>()))
            .thenReturn(mockResponse)

        val validator = HttpUrlAccessibilityValidator(mockClient)
        assertDoesNotThrow { validator.validate("https://valid-site.org/resource") }
        assertTrue(validator.isValid("https://valid-site.org/resource"))
    }

    @Test
    fun `AC1 - accessible URL with 3xx redirection is accepted`() {
        val mockClient = mock(HttpClient::class.java)
        @Suppress("UNCHECKED_CAST")
        val mockResponse = mock(HttpResponse::class.java) as HttpResponse<Void>
        `when`(mockResponse.statusCode()).thenReturn(301)
        `when`(mockClient.send(any(HttpRequest::class.java), any<HttpResponse.BodyHandler<Void>>()))
            .thenReturn(mockResponse)

        val validator = HttpUrlAccessibilityValidator(mockClient)
        assertDoesNotThrow { validator.validate("https://redirect-site.org/home") }
    }

    @Test
    fun `AC2 - dead URL returning 404 throws UrlNotReachableException`() {
        val mockClient = mock(HttpClient::class.java)
        @Suppress("UNCHECKED_CAST")
        val mockResponse = mock(HttpResponse::class.java) as HttpResponse<Void>
        `when`(mockResponse.statusCode()).thenReturn(404)
        `when`(mockClient.send(any(HttpRequest::class.java), any<HttpResponse.BodyHandler<Void>>()))
            .thenReturn(mockResponse)

        val validator = HttpUrlAccessibilityValidator(mockClient)
        val ex =
            assertThrows(UrlNotReachableException::class.java) {
                validator.validate("https://dead-site.org/missing")
            }
        assertTrue(ex.message!!.contains("404"))
        assertFalse(validator.isValid("https://dead-site.org/missing"))
    }

    @Test
    fun `AC2 - server error returning 500 throws UrlNotReachableException`() {
        val mockClient = mock(HttpClient::class.java)
        @Suppress("UNCHECKED_CAST")
        val mockResponse = mock(HttpResponse::class.java) as HttpResponse<Void>
        `when`(mockResponse.statusCode()).thenReturn(500)
        `when`(mockClient.send(any(HttpRequest::class.java), any<HttpResponse.BodyHandler<Void>>()))
            .thenReturn(mockResponse)

        val validator = HttpUrlAccessibilityValidator(mockClient)
        val ex =
            assertThrows(UrlNotReachableException::class.java) {
                validator.validate("https://server-error.org/crash")
            }
        assertTrue(ex.message!!.contains("500"))
    }

    @Test
    fun `AC3 - connect timeout throws UrlNotReachableException`() {
        val mockClient = mock(HttpClient::class.java)
        `when`(mockClient.send(any(HttpRequest::class.java), any<HttpResponse.BodyHandler<Void>>()))
            .thenThrow(HttpConnectTimeoutException("connect timed out"))

        val validator = HttpUrlAccessibilityValidator(mockClient)
        val ex =
            assertThrows(UrlNotReachableException::class.java) {
                validator.validate("https://slow-site.org/timeout")
            }
        assertEquals("Connection timeout", ex.reason)
    }

    @Test
    fun `AC3 - request timeout throws UrlNotReachableException`() {
        val mockClient = mock(HttpClient::class.java)
        `when`(mockClient.send(any(HttpRequest::class.java), any<HttpResponse.BodyHandler<Void>>()))
            .thenThrow(HttpTimeoutException("request timed out"))

        val validator = HttpUrlAccessibilityValidator(mockClient)
        val ex =
            assertThrows(UrlNotReachableException::class.java) {
                validator.validate("https://slow-site.org/read-timeout")
            }
        assertEquals("Request timeout", ex.reason)
    }

    @Test
    fun `AC3 - network IO error throws UrlNotReachableException`() {
        val mockClient = mock(HttpClient::class.java)
        `when`(mockClient.send(any(HttpRequest::class.java), any<HttpResponse.BodyHandler<Void>>()))
            .thenThrow(IOException("DNS lookup failed"))

        val validator = HttpUrlAccessibilityValidator(mockClient)
        val ex =
            assertThrows(UrlNotReachableException::class.java) {
                validator.validate("https://unresolvable-domain.org")
            }
        assertTrue(ex.reason.contains("DNS lookup failed"))
    }

    @Test
    fun `AC4 - HEAD method 405 falls back to GET successfully`() {
        val mockClient = mock(HttpClient::class.java)
        @Suppress("UNCHECKED_CAST")
        val headResponse = mock(HttpResponse::class.java) as HttpResponse<Void>
        @Suppress("UNCHECKED_CAST")
        val getResponse = mock(HttpResponse::class.java) as HttpResponse<Void>
        `when`(headResponse.statusCode()).thenReturn(405)
        `when`(getResponse.statusCode()).thenReturn(200)

        `when`(mockClient.send(any(HttpRequest::class.java), any<HttpResponse.BodyHandler<Void>>()))
            .thenReturn(headResponse)
            .thenReturn(getResponse)

        val validator = HttpUrlAccessibilityValidator(mockClient)
        assertDoesNotThrow { validator.validate("https://blocks-head.org/page") }
    }

    @Test
    fun `AC4 - HEAD method 405 falls back to GET and fails if GET returns 404`() {
        val mockClient = mock(HttpClient::class.java)
        @Suppress("UNCHECKED_CAST")
        val headResponse = mock(HttpResponse::class.java) as HttpResponse<Void>
        @Suppress("UNCHECKED_CAST")
        val getResponse = mock(HttpResponse::class.java) as HttpResponse<Void>
        `when`(headResponse.statusCode()).thenReturn(405)
        `when`(getResponse.statusCode()).thenReturn(404)

        `when`(mockClient.send(any(HttpRequest::class.java), any<HttpResponse.BodyHandler<Void>>()))
            .thenReturn(headResponse)
            .thenReturn(getResponse)

        val validator = HttpUrlAccessibilityValidator(mockClient)
        assertThrows(UrlNotReachableException::class.java) {
            validator.validate("https://blocks-head-and-missing.org/page")
        }
    }

    @Test
    fun `AC5 - malformed syntax fails immediately without network call`() {
        val mockClient = mock(HttpClient::class.java)
        val validator = HttpUrlAccessibilityValidator(mockClient)

        assertThrows(InvalidUrlException::class.java) { validator.validate("ftp://not-supported.org") }
        assertThrows(InvalidUrlException::class.java) { validator.validate("just-a-plain-string") }
        assertThrows(InvalidUrlException::class.java) { validator.validate("http://") }
    }

    @Test
    fun `POST link with invalid syntax returns 400 ProblemDetail RFC 9457`() {
        mockMvc
            .perform(
                post("/api/link")
                    .contentType(APPLICATION_FORM_URLENCODED)
                    .param("url", "not-a-valid-url"),
            ).andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.title").value("Invalid URL"))
    }

    @Test
    fun `POST link with valid reserved documentation domain returns 201 Created`() {
        mockMvc
            .perform(
                post("/api/link")
                    .contentType(APPLICATION_FORM_URLENCODED)
                    .param("url", "https://example.com/ac1-test"),
            ).andExpect(status().isCreated)
            .andExpect(header().exists("Location"))
            .andExpect(jsonPath("$.hash").isString)
    }
}
