package com.bhkpo.vacation.domain.vacation

import com.bhkpo.vacation.domain.BaseQueryRepository
import com.bhkpo.vacation.domain.BaseSyncRepository

interface FixedVacationRepository
    : BaseQueryRepository<FixedVacationEntity, FixedVacationSearchCondition, Long>,
    BaseSyncRepository<FixedVacationEntity, Long>
