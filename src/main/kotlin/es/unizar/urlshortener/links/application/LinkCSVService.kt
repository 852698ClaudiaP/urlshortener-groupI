package es.unizar.urlshortener.links.application

import es.unizar.urlshortener.links.CSVUploadedEvent
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets.UTF_8
import java.util.UUID

@Service
class LinkCSVService(
    private val createShortUrl: CreateShortUrl,
    private val events: ApplicationEventPublisher,
) {
    private val logger = LoggerFactory.getLogger(LinkCSVService::class.java)

    private data class ProcessingStats(
        var totalRows: Int = 0,
        var successfulRows: Int = 0,
        var failedRows: Int = 0,
    )

    private data class RowResult(
        val shortUrl: String,
        val status: String,
    )

    fun processCsv(input: InputStream): ByteArray {
        val output = ByteArrayOutputStream()
        val stats = ProcessingStats()

        processInput(input, output, stats)
        publishEvent(stats)

        return output.toByteArray()
    }

    private fun processInput(
        input: InputStream,
        output: ByteArrayOutputStream,
        stats: ProcessingStats,
    ) {
        InputStreamReader(input, UTF_8).use { reader ->
            val parser =
                CSVFormat.RFC4180
                    .builder()
                    .setHeader()
                    .setSkipHeaderRecord(true)
                    .get()
                    .parse(reader)

            parser.use {
                val headers = parser.headerNames

                require("url" in headers) {
                    "CSV must contain a 'url' column"
                }

                writeOutput(parser, output, headers, stats)
            }
        }
    }

    private fun writeOutput(
        parser: org.apache.commons.csv.CSVParser,
        output: ByteArrayOutputStream,
        headers: List<String>,
        stats: ProcessingStats,
    ) {
        val outputHeaders = headers + listOf("shortUrl", "status")

        OutputStreamWriter(output, UTF_8).use { writer ->
            val printer =
                CSVPrinter(
                    writer,
                    CSVFormat.RFC4180
                        .builder()
                        .setHeader(*outputHeaders.toTypedArray())
                        .get(),
                )
            printer.use {
                parser.forEach { record ->
                    processRecord(record, headers, printer, stats)
                }
            }
        }
    }

    private fun processRecord(
        record: org.apache.commons.csv.CSVRecord,
        headers: List<String>,
        printer: CSVPrinter,
        stats: ProcessingStats,
    ) {
        stats.totalRows++

        val originalValues = headers.map { record.get(it) }
        val url = record.get("url").trim()
        val result = createUrl(url, stats)

        printer.printRecord(
            originalValues + listOf(result.shortUrl, result.status),
        )
    }

    private fun createUrl(
        url: String,
        stats: ProcessingStats,
    ): RowResult =
        try {
            val created = createShortUrl.create(url)
            stats.successfulRows++

            RowResult(
                shortUrl = created.hash,
                status = "CREATED",
            )
        } catch (e: InvalidUrlException) {
            handleFailure(stats, e)
        } catch (e: UrlNotReachableException) {
            handleFailure(stats, e)
        }

    private fun handleFailure(
        stats: ProcessingStats,
        exception: Exception,
    ): RowResult {
        stats.failedRows++

        logger.warn("Failed to create short URL", exception)

        return RowResult(
            shortUrl = "",
            status = "ERROR",
        )
    }

    private fun publishEvent(stats: ProcessingStats) {
        events.publishEvent(
            CSVUploadedEvent(
                uploadId = UUID.randomUUID(),
                totalRows = stats.totalRows,
                successfulRows = stats.successfulRows,
                failedRows = stats.failedRows,
            ),
        )
    }
}
