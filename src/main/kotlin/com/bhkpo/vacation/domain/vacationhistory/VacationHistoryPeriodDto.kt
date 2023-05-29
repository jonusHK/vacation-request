package com.bhkpo.vacation.domain.vacationhistory

import java.time.LocalDateTime

data class VacationHistoryPeriodDto(

    var days: Float,

    var startAt: LocalDateTime,

    var endAt: LocalDateTime
)
