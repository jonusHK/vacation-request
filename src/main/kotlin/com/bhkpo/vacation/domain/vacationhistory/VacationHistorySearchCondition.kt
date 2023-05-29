package com.bhkpo.vacation.domain.vacationhistory

import com.bhkpo.vacation.common.code.VacationHistoryStatus
import com.bhkpo.vacation.common.code.VacationHistoryType
import java.time.LocalDateTime

data class VacationHistorySearchCondition(
    val memberEmail: String,

    val vacationId: Long? = null,

    val type: VacationHistoryType? = null,

    val status: VacationHistoryStatus? = null,

    val startAtLoe: LocalDateTime? = null,

    val startAtGoe: LocalDateTime? = null,

    val endAtLoe: LocalDateTime? = null,

    val endAtGoe: LocalDateTime? = null
)
