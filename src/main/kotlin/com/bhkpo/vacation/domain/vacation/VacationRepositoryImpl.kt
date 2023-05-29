package com.bhkpo.vacation.domain.vacation

import com.bhkpo.vacation.domain.BaseSyncRepositoryImpl
import com.bhkpo.vacation.domain.vacation.QVacationEntity.vacationEntity
import com.querydsl.core.BooleanBuilder
import com.querydsl.jpa.impl.JPAQueryFactory
import jakarta.persistence.EntityManager
import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.Lock
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Transactional(readOnly = true)
@Repository
class VacationRepositoryImpl(
    private val em: EntityManager,
    private val queryFactory: JPAQueryFactory
) : VacationRepository,
    BaseSyncRepositoryImpl<VacationEntity, Long>(em, VacationEntity::class.java) {

    @Lock(LockModeType.OPTIMISTIC)
    override fun findById(id: Long): VacationEntity? {
        return queryFactory
            .selectFrom(vacationEntity)
            .join(vacationEntity.member).fetchJoin()
            .where(vacationEntity.id.eq(id))
            .fetchOne()
    }

    override fun countByCondition(condition: VacationSearchCondition): Long {
        return queryFactory
            .select(vacationEntity.count())
            .from(vacationEntity)
            .join(vacationEntity.member)
            .where(generateBuilder(condition))
            .fetch()[0]
    }

    override fun searchByCondition(condition: VacationSearchCondition, offset: Long, limit: Long): List<VacationEntity>? {
        return queryFactory
            .selectFrom(vacationEntity)
            .join(vacationEntity.member).fetchJoin()
            .where(generateBuilder(condition))
            .offset(offset)
            .limit(limit)
            .orderBy(vacationEntity.createdAt.desc())
            .fetch()
    }

    override fun existsByCondition(condition: VacationSearchCondition): Boolean {
        return queryFactory
            .selectFrom(vacationEntity)
            .where(generateBuilder(condition))
            .join(vacationEntity.member)
            .fetchFirst() != null
    }

    private fun generateBuilder(condition: VacationSearchCondition): BooleanBuilder {
        val builder = BooleanBuilder(vacationEntity.member.email.eq(condition.memberEmail))

        condition.type?.let {
            builder.and(vacationEntity.type.eq(it.code))
        }
        return builder
    }
}