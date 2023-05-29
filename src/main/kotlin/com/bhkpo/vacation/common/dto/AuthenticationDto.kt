package com.bhkpo.vacation.common.dto

data class AuthenticationDto(

    val email: String,

    val authorities: List<String> = listOf()
)
