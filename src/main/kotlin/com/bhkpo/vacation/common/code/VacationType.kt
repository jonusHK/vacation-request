package com.bhkpo.vacation.common.code

enum class VacationType(val code: String, val label: String) {

    FIXED("FIXED", "고정 휴가");

    companion object {
        @JvmStatic
        fun getByCode(code: String): VacationType? = values().find { it.code == code }
    }
}
