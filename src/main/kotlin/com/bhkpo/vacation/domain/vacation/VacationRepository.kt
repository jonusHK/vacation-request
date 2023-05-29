package com.bhkpo.vacation.domain.vacation

import com.bhkpo.vacation.domain.BaseQueryRepository
import com.bhkpo.vacation.domain.BaseSyncRepository

interface VacationRepository
    : BaseQueryRepository<VacationEntity, VacationSearchCondition, Long>,
    BaseSyncRepository<VacationEntity, Long>
