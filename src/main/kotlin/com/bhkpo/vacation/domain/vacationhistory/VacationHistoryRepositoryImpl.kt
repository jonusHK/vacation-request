package com.bhkpo.vacation.domain.vacationhistory

import com.bhkpo.vacation.domain.BaseSyncRepositoryImpl
import com.bhkpo.vacation.domain.auth.QMemberEntity.memberEntity
import com.bhkpo.vacation.domain.vacation.QVacationEntity.vacationEntity
import com.bhkpo.vacation.domain.vacationhistory.QVacationHistoryEntity.vacationHistoryEntity
import com.querydsl.core.BooleanBuilder
import com.querydsl.jpa.impl.JPAQueryFactory
import jakarta.persistence.EntityManager
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Transactional(readOnly = true)
@Repository
class VacationHistoryRepositoryImpl(
    private val em: EntityManager,
    private val queryFactory: JPAQueryFactory
) : VacationHistoryRepository,
    BaseSyncRepositoryImpl<VacationHistoryEntity, Long>(em, VacationHistoryEntity::class.java) {

    override fun findById(id: Long): VacationHistoryEntity? {
        return queryFactory
            .selectFrom(vacationHistoryEntity)
            .where(vacationHistoryEntity.id.eq(id))
            .fetchOne()
    }

    override fun countByCondition(condition: VacationHistorySearchCondition): Long {
        return queryFactory
            .select(vacationHistoryEntity.count())
            .from(vacationHistoryEntity)
            .where(generateBuilder(condition))
            .fetch()[0]
    }

    override fun searchByCondition(
        condition: VacationHistorySearchCondition,
        offset: Long,
        limit: Long
    ): List<VacationHistoryEntity>? {
        return queryFactory
            .selectFrom(vacationHistoryEntity)
            .where(generateBuilder(condition))
            .join(vacationHistoryEntity.vacation).fetchJoin()
            .offset(offset)
            .limit(limit)
            .orderBy(vacationHistoryEntity.startAt.desc())
            .fetch()
    }

    override fun existsByCondition(condition: VacationHistorySearchCondition): Boolean {
        return queryFactory
            .selectFrom(vacationHistoryEntity)
            .where(generateBuilder(condition))
            .join(vacationHistoryEntity.vacation, vacationEntity).fetchJoin()
            .join(vacationEntity.member, memberEntity).fetchJoin()
            .fetchFirst() != null
    }

    private fun generateBuilder(condition: VacationHistorySearchCondition): BooleanBuilder {
        val builder = BooleanBuilder(
            vacationHistoryEntity.vacation.member.email.eq(condition.memberEmail)
        )

        condition.vacationId?.let {
            builder.and(vacationHistoryEntity.vacation.id.eq(it))
        }
        condition.type?.let {
            builder.and(vacationHistoryEntity.type.eq(it))
        }
        condition.status?.let {
            builder.and(vacationHistoryEntity.status.eq(it))
        }
        condition.startAtLoe?.let {
            builder.and(vacationHistoryEntity.startAt.loe(it))
        }
        condition.startAtGoe?.let {
            builder.and(vacationHistoryEntity.startAt.goe(it))
        }
        condition.endAtLoe?.let {
            builder.and(vacationHistoryEntity.endAt.loe(it))
        }
        condition.endAtGoe?.let {
            builder.and(vacationHistoryEntity.endAt.goe(it))
        }

        return builder
    }
}
