package com.bhkpo.vacation.domain.auth

import com.bhkpo.vacation.domain.BaseSyncRepositoryImpl
import com.bhkpo.vacation.domain.auth.QMemberEntity.memberEntity
import com.querydsl.core.BooleanBuilder
import com.querydsl.jpa.impl.JPAQueryFactory
import jakarta.persistence.EntityManager
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Transactional(readOnly = true)
@Repository
class MemberRepositoryImpl(
    private val em: EntityManager,
    private val queryFactory: JPAQueryFactory
) : MemberRepository, BaseSyncRepositoryImpl<MemberEntity, Long>(em, MemberEntity::class.java) {

    override fun findById(id: Long): MemberEntity? {
        return queryFactory
            .selectFrom(memberEntity)
            .where(memberEntity.id.eq(id))
            .fetchOne()
    }

    override fun findByEmail(email: String): MemberEntity? {
        return queryFactory
            .selectFrom(memberEntity)
            .where(memberEntity.email.eq(email))
            .fetchOne()
    }

    override fun countByCondition(condition: MemberSearchCondition): Long {
        return queryFactory
            .select(memberEntity.count())
            .from(memberEntity)
            .where(generateBuilder(condition))
            .fetch()[0]
    }

    override fun searchByCondition(condition: MemberSearchCondition, offset: Long, limit: Long): List<MemberEntity>? {
        return queryFactory
            .selectFrom(memberEntity)
            .where(generateBuilder(condition))
            .offset(offset)
            .limit(limit)
            .orderBy(memberEntity.createdAt.desc())
            .fetch()
    }

    override fun existsByCondition(condition: MemberSearchCondition): Boolean {
        return queryFactory
            .selectFrom(memberEntity)
            .where(generateBuilder(condition))
            .fetchFirst() != null
    }

    private fun generateBuilder(condition: MemberSearchCondition): BooleanBuilder {
        val builder = BooleanBuilder()

        condition.email?.let {
            builder.and(memberEntity.email.eq(it))
        }

        return builder
    }
}
