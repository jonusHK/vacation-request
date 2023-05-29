package com.bhkpo.vacation.application.port.inbound.auth

import com.bhkpo.vacation.common.dto.AuthenticationDto

interface ExtractAuthenticationUseCase {

    fun extractAuthentication(): AuthenticationDto?
}
