package com.bhkpo.vacation.common.dto

import java.time.LocalDateTime

data class MemberDto(

    val id: Long,

    val email: String,

    val accessToken: String,

    val lastLoginAt: LocalDateTime? = null
)
