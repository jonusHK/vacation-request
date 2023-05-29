package com.bhkpo.vacation.adapter.inbound.presentation.dto.request

import com.bhkpo.vacation.adapter.inbound.presentation.validator.ValidRequestVacationHistory
import com.bhkpo.vacation.common.code.VacationHistoryType
import java.time.LocalDateTime

@ValidRequestVacationHistory
data class RequestVacationHistoryDto(

    val vacationId: Long,

    val type: String,

    val startAt: LocalDateTime,

    val endAt: LocalDateTime? = null,

    val days: Int? = null,

    val comment: String? = null

) {
    val typeEnum: VacationHistoryType
        get() = VacationHistoryType.getByCode(type)!!
}
