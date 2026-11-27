package es.unizar.urlshortener.links

import java.time.Instant
import java.util.UUID

/**
 * Published after a short URL is persisted.
 */
data class CSVUploadedEvent(
    val uploadId: UUID,
    val totalRows: Int,
    val successfulRows: Int,
    val failedRows: Int,
    val occurredAt: Instant = Instant.now(),
)
