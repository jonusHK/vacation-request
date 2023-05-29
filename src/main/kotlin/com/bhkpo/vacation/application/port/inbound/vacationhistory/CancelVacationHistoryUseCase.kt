package com.bhkpo.vacation.application.port.inbound.vacationhistory

import com.bhkpo.vacation.common.dto.AuthenticationDto
import com.bhkpo.vacation.common.dto.CancelVacationHistoryDto

interface CancelVacationHistoryUseCase {

    fun cancel(authentication: AuthenticationDto, id: Long): CancelVacationHistoryDto
}
