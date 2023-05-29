package com.bhkpo.vacation.common.code

enum class VacationHistoryType(val code: String, val label: String, val value: Float) {

    DAY("DAY", "연차", 1.0F),

    HALF("HALF", "반차", 0.5F),

    QUARTER("QUARTER", "반반차", 0.25F);

    companion object {
        @JvmStatic
        fun getByCode(code: String): VacationHistoryType? = values().find { it.code == code }
    }
}
