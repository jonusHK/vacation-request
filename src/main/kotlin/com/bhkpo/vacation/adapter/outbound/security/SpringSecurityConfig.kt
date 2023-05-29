package com.bhkpo.vacation.adapter.outbound.security

import org.springframework.boot.autoconfigure.security.servlet.PathRequest.toH2Console
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.security.web.csrf.CsrfFilter
import org.springframework.web.filter.CharacterEncodingFilter


@Configuration
@EnableWebSecurity
class SpringSecurityConfig(
    private val jwtAccessDeniedHandler: JwtAccessDeniedHandler,
    private val jwtAuthenticationEntryPoint: JwtAuthenticationEntryPoint,
    private val jwtFilter: JwtFilter
) {

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }

    @Bean
    fun configure(http: HttpSecurity): SecurityFilterChain {

        val filter = CharacterEncodingFilter().apply {
            this.encoding = "UTF-8"
            this.setForceEncoding(true)
        }

        http.csrf().disable()
            .cors()
                .and()

            .addFilterBefore(filter, CsrfFilter::class.java)
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter::class.java)

            // 인증, 권한 에러 핸들링
            .exceptionHandling()
                .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                .accessDeniedHandler(jwtAccessDeniedHandler)
                .and()

            // H2 콘솔 창 조회
            .headers()
                .frameOptions().sameOrigin()
                .and()

            // 세션 STATELESS 설정
            .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()

            // API 경로
            .authorizeHttpRequests()
                .requestMatchers(toH2Console()).permitAll()
                .requestMatchers("/api/v1/auth/**").permitAll()
                .requestMatchers("/api/**").authenticated()
                .anyRequest().permitAll()
                .and()

        return http.build()
    }
}
