package es.unizar.urlshortener.links

import es.unizar.urlshortener.links.application.CreateShortUrl
import es.unizar.urlshortener.links.application.CreatedShortUrl
import es.unizar.urlshortener.links.application.LinkCSVService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationEventPublisher

@SpringBootTest
@ExtendWith(MockitoExtension::class)
class LinkCSVTests {
    @Mock
    private lateinit var createShortUrl: CreateShortUrl

    @Mock
    private lateinit var events: ApplicationEventPublisher

    private lateinit var service: LinkCSVService

    @BeforeEach
    fun setUp() {
        service =
            LinkCSVService(
                createShortUrl = createShortUrl,
                events = events,
            )
    }

    @Test
    @Suppress("LongMethod")
    fun `should generate CSV with original data and shortened URLs`() {
        val csvInput =
            """
            name,url
            Google,https://www.google.com
            GitHub,https://github.com
            """.trimIndent()

        `when`(createShortUrl.create("https://www.google.com"))
            .thenReturn(
                CreatedShortUrl(
                    hash = "abc123",
                    target = "https://www.google.com",
                ),
            )

        `when`(createShortUrl.create("https://github.com"))
            .thenReturn(
                CreatedShortUrl(
                    hash = "def456",
                    target = "https://github.com",
                ),
            )

        val result =
            service
                .processCsv(
                    csvInput.byteInputStream(),
                ).toString(Charsets.UTF_8)

        val expectedCsv =
            """
            name,url,shortUrl,status
            Google,https://www.google.com,abc123,CREATED
            GitHub,https://github.com,def456,CREATED
            """.trimIndent()

        assertEquals(expectedCsv.trim().replace("\r\n", "\n"), result.trim().replace("\r\n", "\n"))
    }
}
