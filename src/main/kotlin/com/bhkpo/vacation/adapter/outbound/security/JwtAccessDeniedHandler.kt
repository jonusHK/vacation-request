package com.bhkpo.vacation.adapter.outbound.security

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.bhkpo.vacation.adapter.ErrorResponse
import com.bhkpo.vacation.adapter.ResponseCode.ErrorCode.FORBIDDEN
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.MediaType
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.web.access.AccessDeniedHandler
import org.springframework.stereotype.Component

@Component
class JwtAccessDeniedHandler: AccessDeniedHandler {
    private val objectMapper = jacksonObjectMapper()

    override fun handle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        accessDeniedException: AccessDeniedException
    ) {
        // 403 (권한 없음)
        val errorCode = FORBIDDEN

        response.status = errorCode.status
        response.contentType = MediaType.APPLICATION_JSON_VALUE

        objectMapper.writeValue(
            response.writer,
            ErrorResponse.withCode(errorCode, accessDeniedException.message)
        )
    }
}
