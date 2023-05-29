package com.bhkpo.vacation.adapter.inbound.presentation

import com.bhkpo.vacation.adapter.inbound.presentation.dto.request.AuthLoginDto
import com.bhkpo.vacation.adapter.inbound.presentation.dto.request.AuthSignupDto
import com.bhkpo.vacation.adapter.inbound.presentation.dto.response.AuthLoginSuccessDto
import com.bhkpo.vacation.adapter.inbound.presentation.dto.response.AuthSignupSuccessDto
import com.bhkpo.vacation.application.port.inbound.auth.ExtractHeaderTokenUseCase
import com.bhkpo.vacation.application.port.inbound.auth.JoinMemberUseCase
import com.bhkpo.vacation.application.port.inbound.auth.LoginMemberUseCase
import com.bhkpo.vacation.common.dto.HeaderTokenInfoDto
import com.bhkpo.vacation.common.dto.MemberDto
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/auth")
@Validated
class AuthController(
    private val joinMemberUseCase: JoinMemberUseCase,
    private val loginMemberUseCase: LoginMemberUseCase,
    private val extractHeaderTokenUseCase: ExtractHeaderTokenUseCase
) {

    private val log = LoggerFactory.getLogger(this.javaClass)

    /**
     * 회원 가입
     *
     * @param: email, password
     * @return email
     */
    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    fun signup(@Validated @RequestBody data: AuthSignupDto): AuthSignupSuccessDto {

        // 회원 가입
        val memberId: Long = joinMemberUseCase.join(email = data.email, password = data.password)

        return AuthSignupSuccessDto(id = memberId, email = data.email)
    }

    /**
     * 로그인
     *
     * @param: email, password
     * @return email, accessToken
     */
    @PostMapping("/login")
    fun login(@Validated @RequestBody data: AuthLoginDto, response: HttpServletResponse): AuthLoginSuccessDto {

        // 로그인
        val member: MemberDto = loginMemberUseCase.login(email = data.email, password = data.password)

        log.debug("로그인 성공. email={}", data.email)

        // 응답 헤더 토큰 업데이트
        val headerInfo: HeaderTokenInfoDto = extractHeaderTokenUseCase.extractHeaderInfo()
        response.setHeader(headerInfo.headerName, headerInfo.headerValuePrefix + member.accessToken)

        log.debug("응답 헤더 토큰 업데이트 완료. token={}", member.accessToken)

        return AuthLoginSuccessDto(
            id = member.id,
            email = member.email,
            accessToken = member.accessToken
        )
    }
}
