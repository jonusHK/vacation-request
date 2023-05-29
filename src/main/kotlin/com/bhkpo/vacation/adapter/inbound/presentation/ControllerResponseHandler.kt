package com.bhkpo.vacation.adapter.inbound.presentation

import com.bhkpo.vacation.adapter.ErrorResponse
import com.bhkpo.vacation.adapter.ResponseCode
import com.bhkpo.vacation.adapter.SuccessResponse
import com.bhkpo.vacation.common.exception.*
import org.slf4j.LoggerFactory
import org.springframework.core.MethodParameter
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.server.ServerHttpRequest
import org.springframework.http.server.ServerHttpResponse
import org.springframework.http.server.ServletServerHttpResponse
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.AuthenticationException
import org.springframework.validation.BindException
import org.springframework.validation.ObjectError
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice

@RestControllerAdvice
class ControllerResponseHandler: ResponseBodyAdvice<Any> {

    private val log = LoggerFactory.getLogger(this.javaClass)

    override fun supports(returnType: MethodParameter, converterType: Class<out HttpMessageConverter<*>>): Boolean {
        return true
    }

    override fun beforeBodyWrite(
        body: Any?,
        returnType: MethodParameter,
        selectedContentType: MediaType,
        selectedConverterType: Class<out HttpMessageConverter<*>>,
        request: ServerHttpRequest,
        response: ServerHttpResponse
    ): Any? {
        val servletResponse = (response as ServletServerHttpResponse).servletResponse

        // 성공 응답 데이터 변환
        if (selectedContentType == MediaType.APPLICATION_JSON) {
            body?.let {
                if (body !is ErrorResponse) {
                    return SuccessResponse.fromStatus(body, servletResponse.status) ?: body
                }
            }
        }
        return body
    }

    private fun <T> concatenate(vararg lists: List<T>): List<T> {
        val result = mutableListOf<T>()
        lists.forEach { list -> result.addAll(list) }
        return result
    }

    @ExceptionHandler(BindException::class)
    private fun handleValidException(e: BindException): ResponseEntity<ErrorResponse> {

        log.error("Binding 예외 발생 ${e.message}")

        val errorCode: ResponseCode.ErrorCode = ResponseCode.ErrorCode.INVALID
        val errorMessages = mutableListOf<String>()
        val objectErrors: List<ObjectError> = concatenate(
            e.bindingResult.fieldErrors,
            e.bindingResult.globalErrors
        )

        objectErrors.forEach { f ->
            f.defaultMessage?.let {
                errorMessages.add(it)
            }
        }

        return ResponseEntity(
            ErrorResponse.withCode(
                errorCode,
                errorMessages.joinToString(" ")
            ),
            HttpStatus.valueOf(errorCode.status)
        )
    }

    @ExceptionHandler(Exception::class)
    private fun handle(e: Exception): ResponseEntity<ErrorResponse> {

        log.error("예외 발생 ${e.message}")

        // 정의 가능한 예외 코드 변환 (그 외 INTERNAL_SERVER_ERROR)
        val errorCode = when (e) {
            is IllegalStateException -> ResponseCode.ErrorCode.INVALID
            is IllegalArgumentException -> ResponseCode.ErrorCode.INVALID
            is HttpRequestMethodNotSupportedException -> ResponseCode.ErrorCode.METHOD_NOT_ALLOWED
            is AccessDeniedException -> ResponseCode.ErrorCode.FORBIDDEN
            is MemberAccessDeniedException -> ResponseCode.ErrorCode.FORBIDDEN
            is InvalidTokenException -> ResponseCode.ErrorCode.UNAUTHORIZED
            is AuthenticationException -> ResponseCode.ErrorCode.UNAUTHORIZED
            is MemberNotExistException -> ResponseCode.ErrorCode.NOT_EXIST_MEMBER
            is VacationNotExistException -> ResponseCode.ErrorCode.NOT_EXIST_VACATION
            is VacationTypeInvalidException -> ResponseCode.ErrorCode.INVALID_VACATION_TYPE
            is VacationRemainingDaysLackException -> ResponseCode.ErrorCode.LACK_VACATION_REMAINING_DAYS
            is VacationAlreadyCreatedException -> ResponseCode.ErrorCode.ALREADY_CREATED_VACATION
            is VacationHistoryNotExistException -> ResponseCode.ErrorCode.NOT_EXIST_VACATION_HISTORY
            is VacationHistoryEndAtNotExistException -> ResponseCode.ErrorCode.NOT_EXIST_VACATION_HISTORY_END_AT
            is VacationHistoryDaysNotExistException -> ResponseCode.ErrorCode.NOT_EXIST_VACATION_HISTORY_DAYS
            is VacationHistoryTypeInvalidException -> ResponseCode.ErrorCode.INVALID_VACATION_HISTORY_TYPE
            is VacationHistoryStatusInvalidException -> ResponseCode.ErrorCode.INVALID_VACATION_HISTORY_STATUS
            is VacationHistoryDuplicatedPeriodException -> ResponseCode.ErrorCode.DUPLICATED_VACATION_HISTORY_PERIOD
            is VacationHistoryAlreadyStartedException -> ResponseCode.ErrorCode.ALREADY_STARTED_VACATION_HISTORY
            is VacationHistoryAlreadyCanceledException -> ResponseCode.ErrorCode.ALREADY_CANCELED_VACATION_HISTORY
            is VacationHistoryConcurrencyException -> ResponseCode.ErrorCode.CONCURRENCY_VACATION_HISTORY_CREATED
            else -> ResponseCode.ErrorCode.INTERNAL_SERVER_ERROR
        }

        return ResponseEntity(
            ErrorResponse.withCode(errorCode, e.message),
            HttpStatus.valueOf(errorCode.status)
        )
    }
}
