package com.bhkpo.vacation.domain

import jakarta.persistence.EntityManager
import org.springframework.data.jpa.repository.support.JpaEntityInformation
import org.springframework.data.jpa.repository.support.JpaEntityInformationSupport
import org.springframework.transaction.annotation.Transactional

@Transactional(readOnly = true)
class BaseSyncRepositoryImpl<T : Any, ID>(
    private val em: EntityManager,
    private val domainClass: Class<T>
) : BaseSyncRepository<T, ID> {

    private var entityInformation: JpaEntityInformation<T, *> =
        JpaEntityInformationSupport.getEntityInformation(this.domainClass, em)

    @Transactional
    override fun flush() {
        em.flush()
    }

    @Transactional
    override fun save(entity: T): T {
        return if (entityInformation.isNew(entity)) {
            em.persist(entity)
            entity
        } else {
            em.merge(entity)
        }
    }

    @Transactional
    override fun saveAndFlush(entity: T): T {
        val result = save(entity)
        flush()

        return result
    }
}
