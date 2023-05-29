package com.bhkpo.vacation.application.port.inbound.vacation

import com.bhkpo.vacation.common.dto.FixedVacationDto

interface CreateFixedVacationUseCase {

    fun create(memberId: Long, targetYear: Int? = null): FixedVacationDto
}
