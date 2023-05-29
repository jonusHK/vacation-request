package com.bhkpo.vacation.adapter

import com.bhkpo.vacation.adapter.ResponseCode.ErrorCode

data class ErrorResponse(

    val code: String,

    val message: String
) {
    companion object {
        @JvmStatic
        fun withCode(errorCode: ErrorCode, message: String? = null): ErrorResponse {
            return ErrorResponse(errorCode.code, message ?: errorCode.message)
        }
    }
}
