package com.bhkpo.vacation.application.port.outbound.auth

import com.bhkpo.vacation.common.dto.AuthenticationDto

interface ExtractAuthenticationPort {

    fun extractAuthentication(): AuthenticationDto?
}
