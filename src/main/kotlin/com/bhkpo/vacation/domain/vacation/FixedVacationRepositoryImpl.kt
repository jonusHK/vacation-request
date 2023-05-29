package com.bhkpo.vacation.domain.vacation

import com.bhkpo.vacation.domain.BaseSyncRepositoryImpl
import com.bhkpo.vacation.domain.vacation.QFixedVacationEntity.fixedVacationEntity
import com.querydsl.core.BooleanBuilder
import com.querydsl.jpa.impl.JPAQueryFactory
import jakarta.persistence.EntityManager
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Transactional(readOnly = true)
@Repository
class FixedVacationRepositoryImpl(
    private val em: EntityManager,
    private val queryFactory: JPAQueryFactory
) : FixedVacationRepository,
    BaseSyncRepositoryImpl<FixedVacationEntity, Long>(em, FixedVacationEntity::class.java) {

    override fun findById(id: Long): FixedVacationEntity? {
        return queryFactory
            .selectFrom(fixedVacationEntity)
            .join(fixedVacationEntity.member)
            .where(fixedVacationEntity.id.eq(id))
            .fetchOne()
    }

    override fun countByCondition(condition: FixedVacationSearchCondition): Long {
        return queryFactory
            .select(fixedVacationEntity.count())
            .from(fixedVacationEntity)
            .join(fixedVacationEntity.member)
            .where(generateBuilder(condition))
            .fetch()[0]
    }

    override fun searchByCondition(
        condition: FixedVacationSearchCondition,
        offset: Long,
        limit: Long
    ): List<FixedVacationEntity>? {
        return queryFactory
            .selectFrom(fixedVacationEntity)
            .where(generateBuilder(condition))
            .join(fixedVacationEntity.member).fetchJoin()
            .offset(offset)
            .limit(limit)
            .orderBy(fixedVacationEntity.createdAt.desc())
            .fetch()
    }

    override fun existsByCondition(condition: FixedVacationSearchCondition): Boolean {
        return queryFactory
            .selectFrom(fixedVacationEntity)
            .join(fixedVacationEntity.member)
            .where(generateBuilder(condition))
            .fetchFirst() != null
    }

    private fun generateBuilder(condition: FixedVacationSearchCondition): BooleanBuilder {
        val builder = BooleanBuilder(fixedVacationEntity.member.email.eq(condition.memberEmail))

        condition.targetYear?.let {
            builder.and(fixedVacationEntity.targetYear.eq(it))
        }
        return builder
    }
}
