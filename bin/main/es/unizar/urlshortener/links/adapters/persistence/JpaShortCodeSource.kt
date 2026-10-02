package es.unizar.urlshortener.links.adapters.persistence

import es.unizar.urlshortener.links.application.ShortCodeSource
import jakarta.annotation.PostConstruct
import jakarta.persistence.EntityManager
import org.hibernate.engine.spi.SessionFactoryImplementor
import org.springframework.stereotype.Component
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate

@Component
class JpaShortCodeSource(
    private val entityManager: EntityManager,
    transactions: PlatformTransactionManager,
) : ShortCodeSource {
    private val tx = TransactionTemplate(transactions)

    @PostConstruct
    fun createSequenceIfAbsent() {
        tx.executeWithoutResult {
            val present =
                (
                    entityManager
                        .createNativeQuery(
                            "select count(*) from information_schema.sequences where lower(sequence_name) = '$SEQUENCE'",
                        ).singleResult as Number
                ).toLong()
            if (present == 0L) {
                val ddl =
                    dialect()
                        .sequenceSupport
                        .getCreateSequenceString(SEQUENCE, Base62.START.toInt(), 1)
                entityManager.createNativeQuery(ddl).executeUpdate()
            }
        }
    }

    override fun next(): String {
        val sql = dialect().sequenceSupport.getSequenceNextValString(SEQUENCE)
        val id = (entityManager.createNativeQuery(sql).singleResult as Number).toLong()
        return Base62.encode(id)
    }

    private fun dialect() =
        entityManager.entityManagerFactory
            .unwrap(SessionFactoryImplementor::class.java)
            .jdbcServices
            .dialect

    private companion object {
        const val SEQUENCE = "short_url_seq"
    }
}
