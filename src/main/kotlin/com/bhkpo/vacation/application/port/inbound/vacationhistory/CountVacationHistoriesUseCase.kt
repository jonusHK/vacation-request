package com.bhkpo.vacation.application.port.inbound.vacationhistory

import com.bhkpo.vacation.common.code.VacationHistoryStatus
import com.bhkpo.vacation.common.code.VacationHistoryType
import com.bhkpo.vacation.common.dto.AuthenticationDto
import java.time.LocalDateTime

interface CountVacationHistoriesUseCase {

    fun count(
        authentication: AuthenticationDto,
        vacationId: Long? = null,
        type: VacationHistoryType? = null,
        status: VacationHistoryStatus? = null,
        startAtLoe: LocalDateTime? = null,
        startAtGoe: LocalDateTime? = null,
        endAtLoe: LocalDateTime? = null,
        endAtGoe: LocalDateTime? = null
    ): Long
}
