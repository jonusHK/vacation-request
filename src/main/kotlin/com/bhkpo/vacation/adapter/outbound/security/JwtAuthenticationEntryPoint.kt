package com.bhkpo.vacation.adapter.outbound.security

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.bhkpo.vacation.adapter.ErrorResponse
import com.bhkpo.vacation.adapter.ResponseCode.ErrorCode.UNAUTHORIZED
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.MediaType
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.stereotype.Component

@Component
class JwtAuthenticationEntryPoint: AuthenticationEntryPoint {
    private val objectMapper = jacksonObjectMapper()

    override fun commence(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authException: AuthenticationException
    ) {
        // 401 (인증 실패)
        val errorCode = UNAUTHORIZED

        response.status = errorCode.status
        response.contentType = MediaType.APPLICATION_JSON_VALUE

        objectMapper.writeValue(
            response.writer,
            ErrorResponse.withCode(errorCode)
        )
    }
}
