package com.bhkpo.vacation.adapter.inbound.presentation.validator

import com.bhkpo.vacation.adapter.ResponseCode.ErrorCode.INVALID_VACATION_HISTORY_STATUS
import com.bhkpo.vacation.adapter.ResponseCode.ErrorCode.INVALID_VACATION_HISTORY_TYPE
import com.bhkpo.vacation.adapter.inbound.presentation.dto.request.SearchVacationHistoryDto
import com.bhkpo.vacation.common.code.VacationHistoryStatus
import com.bhkpo.vacation.common.code.VacationHistoryType
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import org.slf4j.LoggerFactory

class SearchVacationHistoryValidator : ConstraintValidator<ValidSearchVacationHistory, SearchVacationHistoryDto> {

    private val log = LoggerFactory.getLogger(this.javaClass)

    override fun isValid(value: SearchVacationHistoryDto, context: ConstraintValidatorContext): Boolean {

        var isValid = true
        var message: String? = null

        while (true) {

            // 휴가 신청 유형이 올바르지 않은 경우
            if (!value.type.isNullOrBlank() && VacationHistoryType.getByCode(value.type) == null) {

                log.debug("휴가 신청 유형 값이 올바르지 않습니다. type={}", value.type)

                message = INVALID_VACATION_HISTORY_TYPE.message
                isValid = false
                break
            }

            // 휴가 신청 상태가 올바르지 않은 경우
            if (!value.status.isNullOrBlank() && VacationHistoryStatus.getByCode(value.status) == null) {

                log.debug("휴가 신청 상태 값이 올바르지 않습니다. status={}", value.status)

                message = INVALID_VACATION_HISTORY_STATUS.message
                isValid = false
                break
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
