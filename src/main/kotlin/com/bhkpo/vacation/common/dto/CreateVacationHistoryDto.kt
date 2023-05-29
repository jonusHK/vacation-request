package com.bhkpo.vacation.common.dto

import com.bhkpo.vacation.common.code.VacationHistoryStatus
import com.bhkpo.vacation.common.code.VacationHistoryType
import java.time.LocalDateTime

data class CreateVacationHistoryDto(

    val id: Long,

    val vacationId: Long,

    val remainingDays: Float,

    val type: VacationHistoryType,

    val startAt: LocalDateTime,

    val endAt: LocalDateTime,

    val days: Float,

    val status: VacationHistoryStatus,

    val comment: String? = null
)