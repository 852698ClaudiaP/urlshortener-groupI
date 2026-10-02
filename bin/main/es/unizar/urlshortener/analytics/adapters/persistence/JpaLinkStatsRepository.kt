package es.unizar.urlshortener.analytics.adapters.persistence

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface JpaLinkStatsRepository : JpaRepository<LinkStatsEntity, String> {
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(
        """
        insert into LinkStatsEntity (hash, totalClicks) values (:hash, 1)
        on conflict (hash) do update set totalClicks = totalClicks + 1
        """,
    )
    fun upsertIncrement(
        @Param("hash") hash: String,
    ): Int

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(
        """
        insert into LinkStatsEntity (hash, totalClicks) values (:hash, 0)
        on conflict (hash) do update set totalClicks = totalClicks
        """,
    )
    fun insertIfAbsent(
        @Param("hash") hash: String,
    ): Int
}
