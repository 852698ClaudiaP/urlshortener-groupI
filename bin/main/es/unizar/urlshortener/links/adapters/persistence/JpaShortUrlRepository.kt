package es.unizar.urlshortener.links.adapters.persistence

import org.springframework.data.jpa.repository.JpaRepository

interface JpaShortUrlRepository : JpaRepository<ShortUrlEntity, String>
