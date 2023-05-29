package com.bhkpo.vacation.adapter.outbound.security

import com.bhkpo.vacation.application.port.outbound.auth.CreateTokenPort
import com.bhkpo.vacation.application.port.outbound.auth.ExtractAuthenticationPort
import com.bhkpo.vacation.application.port.outbound.auth.ExtractHeaderTokenPort
import com.bhkpo.vacation.application.port.outbound.auth.PasswordEncodePort
import com.bhkpo.vacation.common.dto.AuthenticationDto
import com.bhkpo.vacation.common.dto.HeaderTokenInfoDto
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component

@Component
class JwtTokenAdapter(
    private val tokenProvider: JwtTokenProvider,
    private val authenticationManagerBuilder: AuthenticationManagerBuilder,
    private val passwordEncoder: PasswordEncoder
): CreateTokenPort, ExtractHeaderTokenPort, ExtractAuthenticationPort, PasswordEncodePort {

    /** 토큰 생성 */
    override fun createToken(email: String, password: String): String {

        val authenticationToken = UsernamePasswordAuthenticationToken(email, password)
        val authentication: Authentication = authenticationManagerBuilder.`object`.authenticate(authenticationToken)

        SecurityContextHolder.getContext().authentication = authentication
        return tokenProvider.createToken(authentication)
    }

    /** 헤더 정보 추출 */
    override fun extractHeaderInfo(): HeaderTokenInfoDto {
        return HeaderTokenInfoDto(JwtFilter.AUTHORIZATION_HEADER, JwtFilter.TOKEN_PREFIX)
    }

    /** 인증 정보 추출 */
    override fun extractAuthentication(): AuthenticationDto? {

        val authentication: Authentication? = SecurityContextHolder.getContext().authentication

        return authentication?.let {
            AuthenticationDto(
                email = authentication.name,
                authorities = authentication.authorities.map { it.authority }
            )
        }
    }

    override fun encode(password: String): String {
        return passwordEncoder.encode(password)
    }
}
