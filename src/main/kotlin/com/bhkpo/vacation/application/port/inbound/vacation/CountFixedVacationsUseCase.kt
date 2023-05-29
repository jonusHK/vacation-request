package com.bhkpo.vacation.application.port.inbound.vacation

import com.bhkpo.vacation.common.dto.AuthenticationDto

interface CountFixedVacationsUseCase {

    fun count(
        authentication: AuthenticationDto,
        targetYear: Int? = null
    ): Long
}
