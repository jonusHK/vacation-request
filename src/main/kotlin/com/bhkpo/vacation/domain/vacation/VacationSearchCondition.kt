package com.bhkpo.vacation.domain.vacation

import com.bhkpo.vacation.common.code.VacationType

data class VacationSearchCondition(

    val memberEmail: String,

    val type: VacationType? = null
)
