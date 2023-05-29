package com.bhkpo.vacation.adapter

import com.bhkpo.vacation.adapter.ResponseCode.SuccessCode

data class SuccessResponse<T>(

    val code: String,

    val data: T
) {
    companion object {

        @JvmStatic
        fun <T> fromStatus(body: T, status: Int): SuccessResponse<T>? {
            val successCode: SuccessCode? = SuccessCode.getByStatus(status)
            return successCode?.let {
                SuccessResponse(successCode.code, body)
            }
        }
    }
}
