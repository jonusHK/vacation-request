package com.bhkpo.vacation.application.port.outbound.auth

import com.bhkpo.vacation.common.dto.HeaderTokenInfoDto

interface ExtractHeaderTokenPort {

    fun extractHeaderInfo(): HeaderTokenInfoDto
}
