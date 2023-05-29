package com.bhkpo.vacation.application.port.inbound.vacation

import com.bhkpo.vacation.common.dto.AuthenticationDto
import com.bhkpo.vacation.common.dto.FixedVacationDto

interface FindFixedVacationByIdUseCase {

    fun findById(authentication: AuthenticationDto, id: Long): FixedVacationDto
}
