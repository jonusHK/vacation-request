package com.bhkpo.vacation.application.port.inbound.vacationhistory

import com.bhkpo.vacation.common.code.VacationHistoryType
import com.bhkpo.vacation.common.dto.AuthenticationDto
import com.bhkpo.vacation.common.dto.CreateVacationHistoryDto
import java.time.LocalDateTime

interface CreateVacationHistoryUseCase {

    fun create(
        authentication: AuthenticationDto,
        vacationId: Long,
        type: VacationHistoryType,
        startAt: LocalDateTime,
        endAt: LocalDateTime? = null,
        days: Float? = null,
        comment: String? = null
    ): CreateVacationHistoryDto
}
