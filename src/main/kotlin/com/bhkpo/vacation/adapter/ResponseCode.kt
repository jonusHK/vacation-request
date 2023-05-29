package com.bhkpo.vacation.adapter

import org.springframework.http.HttpStatus

object ResponseCode {
    enum class SuccessCode(val status: Int, val code: String, val message: String) {

        OK(HttpStatus.OK.value(), "OK", "정상적으로 처리 되었습니다."),

        CREATED(HttpStatus.CREATED.value(), "CREATED", "정상적으로 생성 되었습니다.");

        companion object {
            @JvmStatic
            fun getByStatus(status: Int): SuccessCode? = values().find { it.status == status }
        }
    }

    enum class ErrorCode(val status: Int, val code: String, val message: String) {

        INVALID(HttpStatus.BAD_REQUEST.value(), "INVALID", "유효하지 않은 요청입니다."),

        UNAUTHORIZED(HttpStatus.UNAUTHORIZED.value(), "UNAUTHORIZED", "유효한 인증 자격 증명이 없습니다."),

        FORBIDDEN(HttpStatus.FORBIDDEN.value(), "FORBIDDEN", "권한이 없습니다."),

        NOT_EXIST_MEMBER(HttpStatus.NOT_FOUND.value(), "NOT_EXIST_MEMBER", "회원 정보가 존재하지 않습니다."),

        NOT_EXIST_VACATION(HttpStatus.NOT_FOUND.value(), "NOT_EXIST_VACATION", "휴가 정보가 존재하지 않습니다."),

        INVALID_VACATION_TYPE(HttpStatus.BAD_REQUEST.value(), "INVALID_VACATION_TYPE", "휴가 유형 값이 올바르지 않습니다."),

        NOT_EXIST_VACATION_HISTORY(HttpStatus.NOT_FOUND.value(), "NOT_EXIST_VACATION_HISTORY", "휴가 신청 내역이 존재하지 않습니다."),

        ALREADY_CREATED_VACATION(HttpStatus.BAD_REQUEST.value(), "ALREADY_CREATED_VACATION", "휴가가 이미 생성되어 있습니다."),

        ALREADY_STARTED_VACATION_HISTORY(HttpStatus.BAD_REQUEST.value(), "ALREADY_STARTED_VACATION_HISTORY", "휴가가 이미 시작되었습니다."),

        ALREADY_CANCELED_VACATION_HISTORY(HttpStatus.BAD_REQUEST.value(), "ALREADY_CANCELED_VACATION_HISTORY", "이미 취소된 휴가 신청 건 입니다."),

        INVALID_EMAIL(HttpStatus.BAD_REQUEST.value(), "INVALID_EMAIL", "이메일 형식이 올바르지 않습니다."),

        INVALID_PASSWORD(HttpStatus.BAD_REQUEST.value(), "INVALID_PASSWORD", "비밀번호 형식이 올바르지 않습니다."),

        INVALID_VACATION_HISTORY_TYPE(HttpStatus.BAD_REQUEST.value(), "INVALID_VACATION_HISTORY_TYPE", "휴가 신청 유형 값이 올바르지 않습니다."),

        INVALID_VACATION_HISTORY_STATUS(HttpStatus.BAD_REQUEST.value(), "INVALID_VACATION_HISTORY_STATUS", "휴가 신청 상태 값이 올바르지 않습니다."),

        INVALID_VACATION_HISTORY_START_AT(HttpStatus.BAD_REQUEST.value(), "INVALID_VACATION_HISTORY_START_AT", "휴가 시작일은 현재보다 이후여야 합니다."),

        INVALID_VACATION_HISTORY_END_AT(HttpStatus.BAD_REQUEST.value(), "INVALID_VACATION_HISTORY_END_AT", "휴가 종료일은 시작일보다 이후여야 합니다."),

        INVALID_VACATION_HISTORY_DAYS(HttpStatus.BAD_REQUEST.value(), "INVALID_VACTION_HISTORY_DAYS", "휴가 일수 값이 올바르지 않습니다."),

        NOT_EXIST_VACATION_HISTORY_END_AT(HttpStatus.BAD_REQUEST.value(), "NOT_EXIST_VACATION_HISTORY_END_AT", "휴가 종료일 값이 존재하지 않습니다."),

        NOT_EXIST_VACATION_HISTORY_DAYS(HttpStatus.BAD_REQUEST.value(), "NOT_EXIST_VACATION_HISTORY_DAYS", "휴가 일수 값이 존재하지 않습니다."),

        DUPLICATED_VACATION_HISTORY_PERIOD(HttpStatus.BAD_REQUEST.value(), "DUPLICATED_VACATION_HISTORY_PERIOD", "중복되는 휴가 기간이 있습니다."),

        LACK_VACATION_REMAINING_DAYS(HttpStatus.BAD_REQUEST.value(), "LACK_VACATION_REMAINING_DAYS", "휴가 잔여일수가 부족합니다."),

        CONCURRENCY_VACATION_HISTORY_CREATED(HttpStatus.BAD_REQUEST.value(), "CONCURRENCY_VACATION_HISTORY_CREATED", "동일한 휴가 건에 대해 동시에 신청할 수 없습니다."),

        METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED.value(), "METHOD_NOT_ALLOWED", "허용되지 않은 메소드입니다."),

        INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR.value(), "INTERNAL_SERVER_ERROR", "정의되지 않은 오류");
    }
}
