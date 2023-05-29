package com.bhkpo.vacation.adapter.inbound.presentation.dto.request

import com.bhkpo.vacation.adapter.inbound.presentation.PageInfo
import com.bhkpo.vacation.adapter.inbound.presentation.validator.ValidSearchVacation

@ValidSearchVacation
data class SearchFixedVacationDto(

    val targetYear: Int? = null,

    val offset: Long = PageInfo.OFFSET,

    val limit: Long = PageInfo.LIMIT
)
