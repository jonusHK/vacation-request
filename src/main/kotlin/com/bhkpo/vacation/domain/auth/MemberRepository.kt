package com.bhkpo.vacation.domain.auth

import com.bhkpo.vacation.domain.BaseQueryRepository
import com.bhkpo.vacation.domain.BaseSyncRepository

interface MemberRepository
    : BaseQueryRepository<MemberEntity, MemberSearchCondition, Long>,
    BaseSyncRepository<MemberEntity, Long> {

    fun findByEmail(email: String): MemberEntity?
}
