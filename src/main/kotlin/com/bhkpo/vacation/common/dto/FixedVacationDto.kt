package com.bhkpo.vacation.common.dto

import com.bhkpo.vacation.common.code.VacationType
import java.time.LocalDateTime

data class FixedVacationDto(

    val id: Long,

    val memberId: Long,

    val vacationHistoryIds: List<Long>,

    val targetYear: Int,

    val days: Float,

    val remainingDays: Float,

    val type: VacationType,

    val createdAt: LocalDateTime,
)
