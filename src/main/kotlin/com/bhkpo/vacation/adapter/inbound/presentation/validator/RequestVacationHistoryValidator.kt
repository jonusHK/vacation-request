package com.bhkpo.vacation.adapter.inbound.presentation.validator

import com.bhkpo.vacation.adapter.ResponseCode.ErrorCode.*
import com.bhkpo.vacation.adapter.inbound.presentation.dto.request.RequestVacationHistoryDto
import com.bhkpo.vacation.common.LocalDateTimeUtils.setMaxLocalTime
import com.bhkpo.vacation.common.LocalDateTimeUtils.setMinLocalTime
import com.bhkpo.vacation.common.code.VacationHistoryType
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

class RequestVacationHistoryValidator : ConstraintValidator<ValidRequestVacationHistory, RequestVacationHistoryDto> {

    private val log = LoggerFactory.getLogger(this.javaClass)

    override fun isValid(value: RequestVacationHistoryDto, context: ConstraintValidatorContext): Boolean {

        var isValid = true
        var message: String? = null

        val type: VacationHistoryType? = VacationHistoryType.getByCode(value.type)

        while (true) {

            // 휴가 시작일시를 현재 일시보다 이전으로 입력한 경우
            if (value.startAt < LocalDateTime.now().setMinLocalTime()) {

                log.debug("휴가 시작일시는 현재 일시보다 이전 일 수 없습니다. startAt={}", value.startAt)

                message = INVALID_VACATION_HISTORY_START_AT.message
                isValid = false
                break
            }

            // 휴가 신청 유형(연차/반차/반반차) 이 올바르게 입력되지 않은 경우
            if (type == null) {

                log.debug("휴가 신청 유형이 올바른 값이 아닙니다. type={}", value.type)

                message = INVALID_VACATION_HISTORY_TYPE.message
                isValid = false
                break
            }

            // 연차로 신청한 경우
            if (type == VacationHistoryType.DAY) {

                // 휴가 종료일이 설정되지 않은 경우
                if (value.endAt == null) {

                    log.debug("휴가 종료일이 설정되어 있지 않습니다.")

                    message = NOT_EXIST_VACATION_HISTORY_END_AT.message
                    isValid = false
                    break
                }
                // 휴가 일수가 입력되지 않은 경우
                if (value.days == null) {

                    log.debug("휴가 신청 일수가 입력되어 있지 않습니다.")

                    message = NOT_EXIST_VACATION_HISTORY_DAYS.message
                    isValid = false
                    break
                }

                val startAt = value.startAt.setMinLocalTime()
                val endAt = value.endAt.setMaxLocalTime()

                // 휴가 종료일이 시작일보다 이전인 경우
                if (endAt.isBefore(startAt)) {

                    log.debug("휴가 종료일은 시작일보다 이전 일 수 없습니다.")

                    message = INVALID_VACATION_HISTORY_END_AT.message
                    isValid = false
                    break
                }
                // 신청한 휴가 일수가 시작일과 종료일 사이의 최대 일수를 초과한 경우
                if (
                    endAt.minus(
                        value.days.toLong(), ChronoUnit.DAYS
                    ).plusDays(1).setMinLocalTime() < startAt
                ) {

                    log.debug("휴가 일수가 시작, 종료일 기준 값 보다 크게 입력되었습니다. days={}", value.days)

                    message = INVALID_VACATION_HISTORY_DAYS.message
                    isValid = false
                    break
                }
            }
            break
        }

        if (!isValid && !message.isNullOrBlank()) {
            context.disableDefaultConstraintViolation()
            context.buildConstraintViolationWithTemplate(message).addConstraintViolation()
        }

        return isValid
    }
}
