package es.unizar.urlshortener.analytics.adapters.persistence

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.Instant
import java.util.UUID

interface JpaProcessedClickEventRepository : JpaRepository<ProcessedClickEventEntity, UUID> {
    @Modifying(flushAutomatically = true)
    @Query("insert into ProcessedClickEventEntity (eventId, processedAt) values (:eventId, :processedAt)")
    fun insert(
        @Param("eventId") eventId: UUID,
        @Param("processedAt") processedAt: Instant,
    ): Int

    @Modifying
    @Query("delete from ProcessedClickEventEntity p where p.processedAt < :cutoff")
    fun deleteProcessedBefore(
        @Param("cutoff") cutoff: Instant,
    ): Int
}
