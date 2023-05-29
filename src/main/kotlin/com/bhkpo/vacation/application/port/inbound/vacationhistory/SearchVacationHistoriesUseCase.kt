package com.bhkpo.vacation.application.port.inbound.vacationhistory

import com.bhkpo.vacation.common.code.VacationHistoryStatus
import com.bhkpo.vacation.common.code.VacationHistoryType
import com.bhkpo.vacation.common.dto.AuthenticationDto
import com.bhkpo.vacation.common.dto.VacationHistoryDto
import java.time.LocalDateTime

interface SearchVacationHistoriesUseCase {

    fun search(
        authentication: AuthenticationDto,
        vacationId: Long? = null,
        type: VacationHistoryType? = null,
        status: VacationHistoryStatus? = null,
        startAtLoe: LocalDateTime? = null,
        startAtGoe: LocalDateTime? = null,
        endAtLoe: LocalDateTime? = null,
        endAtGoe: LocalDateTime? = null,
        offset: Long,
        limit: Long
    ): List<VacationHistoryDto>
}
