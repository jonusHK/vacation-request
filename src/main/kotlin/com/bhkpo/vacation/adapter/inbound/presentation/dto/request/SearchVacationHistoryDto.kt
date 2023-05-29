package com.bhkpo.vacation.adapter.inbound.presentation.dto.request

import com.bhkpo.vacation.adapter.inbound.presentation.PageInfo
import com.bhkpo.vacation.adapter.inbound.presentation.validator.ValidSearchVacationHistory
import com.bhkpo.vacation.common.code.VacationHistoryStatus
import com.bhkpo.vacation.common.code.VacationHistoryType
import java.time.LocalDateTime

@ValidSearchVacationHistory
data class SearchVacationHistoryDto(
    val vacationId: Long? = null,
    val type: String? = null,
    val status: String? = null,
    val startAtLoe: LocalDateTime? = null,
    val startAtGoe: LocalDateTime? = null,
    val endAtLoe: LocalDateTime? = null,
    val endAtGoe: LocalDateTime? = null,
    val offset: Long = PageInfo.OFFSET,
    val limit: Long = PageInfo.LIMIT
) {
    val typeEnum: VacationHistoryType?
        get() = type?.let { VacationHistoryType.getByCode(type) }

    val statusEnum: VacationHistoryStatus?
        get() = status?.let { VacationHistoryStatus.getByCode(status) }
}
