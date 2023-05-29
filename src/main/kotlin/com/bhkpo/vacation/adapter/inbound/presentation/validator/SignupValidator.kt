package com.bhkpo.vacation.adapter.inbound.presentation.validator

import com.bhkpo.vacation.adapter.ResponseCode.ErrorCode.INVALID_EMAIL
import com.bhkpo.vacation.adapter.ResponseCode.ErrorCode.INVALID_PASSWORD
import com.bhkpo.vacation.adapter.inbound.presentation.dto.request.AuthSignupDto
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import org.slf4j.LoggerFactory
import java.util.regex.Pattern

class SignupValidator: ConstraintValidator<ValidSignup, AuthSignupDto> {

    private val log = LoggerFactory.getLogger(this.javaClass)

    override fun isValid(value: AuthSignupDto, context: ConstraintValidatorContext): Boolean {

        var isValid = true
        var message: String? = null

        while (true) {

            // 이메일 주소가 빈 공백 및 null 값인지 확인
            if (value.email.isBlank()) {

                log.debug("이메일 주소는 공백이거나 Null 값일 수 없습니다.")

                message = INVALID_EMAIL.message
                isValid = false
                break
            }

            // 이메일 형식 검사
            val pattern = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+.[A-Za-z]{2,6}$")
            if (!pattern.matcher(value.email).find()) {

                log.debug("이메일 주소 형식이 올바르지 않습니다. email={}", value.email)

                message = INVALID_EMAIL.message
                isValid = false
                break
            }

            // 비밀번호가 빈 공백 및 null 값인지 확인
            if (value.password.isBlank()) {

                log.debug("비밀번호는 공백이거나 Null 일 수 없습니다.")

                message = INVALID_PASSWORD.message
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