package com.bhkpo.vacation.adapter.inbound.presentation.dto.response

data class AuthLoginSuccessDto(

    val id: Long,

    val email: String,

    val accessToken: String
)
