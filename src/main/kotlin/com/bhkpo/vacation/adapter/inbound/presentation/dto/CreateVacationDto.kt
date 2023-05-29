package com.bhkpo.vacation.adapter.inbound.presentation.dto

data class CreateVacationDto(

    val memberId: Long,

    val targetYear: Int? = null
)
