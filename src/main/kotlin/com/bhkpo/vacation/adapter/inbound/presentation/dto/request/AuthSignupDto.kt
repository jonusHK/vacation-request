package com.bhkpo.vacation.adapter.inbound.presentation.dto.request

import com.bhkpo.vacation.adapter.inbound.presentation.validator.ValidSignup

@ValidSignup
data class AuthSignupDto(

    val email: String,

    val password: String
)
