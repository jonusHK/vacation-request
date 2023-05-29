package com.bhkpo.vacation.adapter.outbound.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.util.StringUtils
import org.springframework.web.filter.OncePerRequestFilter


@Component
class JwtFilter(private val tokenProvider: JwtTokenProvider): OncePerRequestFilter() {
    private val log = LoggerFactory.getLogger(this.javaClass)

    companion object {
        const val AUTHORIZATION_HEADER = "Authorization"
        const val TOKEN_PREFIX = "Bearer "
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {

        val jwt: String? = extractToken(request)
        val requestURI = request.requestURI

        // 토큰 유효성 검증
        if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt!!)) {
            val authentication: Authentication = tokenProvider.getAuthentication(jwt)
            SecurityContextHolder.getContext().authentication = authentication
            log.debug("Security Context 저장. username={}, uri={}", authentication.name, requestURI)
        } else {
            log.debug("유효한 JWT 토큰이 없습니다. uri={}", requestURI)
        }
        filterChain.doFilter(request, response)
    }

    // 헤더에서 토큰 정보 추출
    private fun extractToken(request: HttpServletRequest): String? {

        val bearerToken = request.getHeader(AUTHORIZATION_HEADER)
        return if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(TOKEN_PREFIX)) {
            bearerToken.substring(TOKEN_PREFIX.length)
        } else null
    }
}
