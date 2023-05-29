package com.bhkpo.vacation.application.service

import com.bhkpo.vacation.application.port.inbound.auth.ExtractAuthenticationUseCase
import com.bhkpo.vacation.application.port.inbound.auth.ExtractHeaderTokenUseCase
import com.bhkpo.vacation.application.port.inbound.auth.JoinMemberUseCase
import com.bhkpo.vacation.application.port.inbound.auth.LoginMemberUseCase
import com.bhkpo.vacation.application.port.inbound.vacation.CreateFixedVacationUseCase
import com.bhkpo.vacation.application.port.outbound.auth.CreateTokenPort
import com.bhkpo.vacation.application.port.outbound.auth.ExtractAuthenticationPort
import com.bhkpo.vacation.application.port.outbound.auth.ExtractHeaderTokenPort
import com.bhkpo.vacation.application.port.outbound.auth.PasswordEncodePort
import com.bhkpo.vacation.common.dto.AuthenticationDto
import com.bhkpo.vacation.common.dto.HeaderTokenInfoDto
import com.bhkpo.vacation.common.dto.MemberDto
import com.bhkpo.vacation.common.exception.MemberNotExistException
import com.bhkpo.vacation.domain.auth.MemberEntity
import com.bhkpo.vacation.domain.auth.MemberRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AuthService(
    private val createTokenPort: CreateTokenPort,
    private val extractHeaderTokenPort: ExtractHeaderTokenPort,
    private val extractAuthenticationPort: ExtractAuthenticationPort,
    private val passwordEncodePort: PasswordEncodePort,
    private val memberRepository: MemberRepository,
    private val createFixedVacationUseCase: CreateFixedVacationUseCase
): JoinMemberUseCase, LoginMemberUseCase, ExtractHeaderTokenUseCase, ExtractAuthenticationUseCase {

    private val log = LoggerFactory.getLogger(this.javaClass)

    /** 회원 가입 */
    @Transactional
    override fun join(email: String, password: String): Long {

        val memberEntity: MemberEntity = generateEntity(
            email = email, password = encodedPassword(password)
        )

        // 회원 엔티티 저장
        memberRepository.save(memberEntity)

        // 고정 휴가 발생 (현재 연도 기준)
        try {
            createFixedVacationUseCase.create(memberEntity.id!!)
        } catch (e: RuntimeException) {
            log.error("고정 휴가 생성 실패 memberId={}, exception={}", memberEntity.id, e.message)
        }

        return memberEntity.id!!
    }

    /** 로그인 */
    @Transactional
    override fun login(email: String, password: String): MemberDto {

        val accessToken: String = createTokenPort.createToken(email, password)
        val memberEntity: MemberEntity = memberRepository.findByEmail(email)
            ?: throw MemberNotExistException()

        memberEntity.login()
        return MemberDto(
            id = memberEntity.id!!,
            email = memberEntity.email,
            accessToken = accessToken,
            lastLoginAt = memberEntity.lastLoginAt
        )
    }

    /** 토큰 헤더 정보 추출 */
    override fun extractHeaderInfo(): HeaderTokenInfoDto {
        return extractHeaderTokenPort.extractHeaderInfo()
    }

    /** 인증 관련 authentication 추출 */
    override fun extractAuthentication(): AuthenticationDto? {
        return extractAuthenticationPort.extractAuthentication()
    }

    /** 패스워드 인코딩 */
    private fun encodedPassword(password: String): String {
        return passwordEncodePort.encode(password)
    }

    private fun generateEntity(email: String, password: String): MemberEntity {
        return MemberEntity(email = email, password = password)
    }
}
