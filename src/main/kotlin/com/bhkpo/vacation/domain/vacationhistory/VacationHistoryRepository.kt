package com.bhkpo.vacation.domain.vacationhistory

import com.bhkpo.vacation.domain.BaseQueryRepository
import com.bhkpo.vacation.domain.BaseSyncRepository

interface VacationHistoryRepository
    : BaseQueryRepository<VacationHistoryEntity, VacationHistorySearchCondition, Long>,
    BaseSyncRepository<VacationHistoryEntity, Long>
