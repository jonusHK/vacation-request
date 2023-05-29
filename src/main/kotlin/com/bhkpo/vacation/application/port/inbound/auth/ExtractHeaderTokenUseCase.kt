package com.bhkpo.vacation.application.port.inbound.auth

import com.bhkpo.vacation.common.dto.HeaderTokenInfoDto

interface ExtractHeaderTokenUseCase {

    fun extractHeaderInfo(): HeaderTokenInfoDto
}
