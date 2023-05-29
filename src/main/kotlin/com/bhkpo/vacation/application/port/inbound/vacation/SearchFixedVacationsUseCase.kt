package com.bhkpo.vacation.application.port.inbound.vacation

import com.bhkpo.vacation.common.dto.AuthenticationDto
import com.bhkpo.vacation.common.dto.FixedVacationDto

interface SearchFixedVacationsUseCase {

    fun search(
        authentication: AuthenticationDto,
        targetYear: Int? = null,
        offset: Long,
        limit: Long
    ): List<FixedVacationDto>
}
