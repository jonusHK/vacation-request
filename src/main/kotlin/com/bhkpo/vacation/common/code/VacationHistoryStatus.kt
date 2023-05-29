package com.bhkpo.vacation.common.code

enum class VacationHistoryStatus(val code: String, val label: String) {

    COMPLETED("COMPLETED", "완료"),

    CANCELED("CANCELED", "취소");

    companion object {
        @JvmStatic
        fun getByCode(code: String): VacationHistoryStatus? = values().find { it.code == code }
    }
}
