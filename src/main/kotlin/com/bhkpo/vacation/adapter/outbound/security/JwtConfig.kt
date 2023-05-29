package com.bhkpo.vacation.adapter.outbound.security

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class JwtConfig {

    @Value("\${jwt.secret}")
    lateinit var accessTokenSecret: String

    @Value("\${jwt.token-validity-in-seconds}")
    var accessTokenValidityInSeconds: Long = 0

    @Bean
    fun tokenProvider(): JwtTokenProvider {
        return JwtTokenProvider(accessTokenSecret, accessTokenValidityInSeconds)
    }
}
