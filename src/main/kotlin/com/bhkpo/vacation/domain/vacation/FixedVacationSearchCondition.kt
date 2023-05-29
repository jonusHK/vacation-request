package com.bhkpo.vacation.domain.vacation

data class FixedVacationSearchCondition(

    val memberEmail: String,

    val targetYear: Int? = null
)
