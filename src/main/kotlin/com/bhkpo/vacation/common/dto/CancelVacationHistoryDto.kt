package com.bhkpo.vacation.common.dto

import com.bhkpo.vacation.common.code.VacationHistoryStatus
import java.time.LocalDateTime

data class CancelVacationHistoryDto(

    val id: Long,

    val vacationId: Long,

    val remainingDays: Float,

    val status: VacationHistoryStatus,

    val canceledAt: LocalDateTime
)
