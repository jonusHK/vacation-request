package com.bhkpo.vacation.application

import com.bhkpo.vacation.ProjectConfig.Companion.createMember
import com.bhkpo.vacation.application.port.inbound.vacation.CreateFixedVacationUseCase
import com.bhkpo.vacation.application.port.outbound.auth.CreateTokenPort
import com.bhkpo.vacation.application.port.outbound.auth.ExtractAuthenticationPort
import com.bhkpo.vacation.application.port.outbound.auth.ExtractHeaderTokenPort
import com.bhkpo.vacation.application.port.outbound.auth.PasswordEncodePort
import com.bhkpo.vacation.application.service.AuthService
import com.bhkpo.vacation.common.dto.MemberDto
import com.bhkpo.vacation.domain.auth.MemberEntity
import com.bhkpo.vacation.domain.auth.MemberRepository
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.comparables.shouldBeLessThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.spyk
import org.junit.jupiter.api.extension.ExtendWith
import java.time.LocalDateTime


@ExtendWith(MockKExtension::class)
internal class AuthServiceTest : DescribeSpec({

    afterContainer {
        clearAllMocks()
    }

    val memberRepository = mockk<MemberRepository>()
    val createTokenPort = mockk<CreateTokenPort>()
    val extractHeaderTokenPort = mockk<ExtractHeaderTokenPort>()
    val extractAuthenticationPort = mockk<ExtractAuthenticationPort>()
    val createFixedVacationUseCase = mockk<CreateFixedVacationUseCase>()
    val passwordEncodePort = mockk<PasswordEncodePort>()
    val authService: AuthService = spyk(
        objToCopy = AuthService(
            createTokenPort,
            extractHeaderTokenPort,
            extractAuthenticationPort,
            passwordEncodePort,
            memberRepository,
            createFixedVacationUseCase
        ),
        recordPrivateCalls = true
    )

    describe("join") {
        context("회원 가입하면") {
            val email = "test@test.com"
            val password = "test"
            val member: MemberEntity = createMember()

            every { authService["encodedPassword"](password) } returns password
            every { authService["generateEntity"](email, password) } returns member
            every { memberRepository.save(member) } returns member

            // 회원가입 로직 실행
            val result: Long = authService.join(email = email, password = password)

            it("회원 ID를 반환하는지 확인한다.") {
                result shouldBe member.id
            }
        }
    }

    describe("login") {
        context("로그인 하면") {
            val email = "test@test.com"
            val password = "test"
            val accessToken = "token"
            val member: MemberEntity = createMember()
            val beforeLoginAt = LocalDateTime.now()

            every { createTokenPort.createToken(email, password) } returns accessToken
            every { memberRepository.findByEmail(email) } returns member

            // 로그인 로직 실행
            val memberDto: MemberDto = authService.login(email, password)

            val afterLoginAt = LocalDateTime.now()

            it("회원의 마지막 로그인 일시가 저장된다.") {
                member.lastLoginAt shouldNotBe null
                member.lastLoginAt?.let {
                    it shouldBeGreaterThan beforeLoginAt
                    it shouldBeLessThan afterLoginAt
                }
            }
            it("정상적으로 데이터를 반환하는지 확인한다.") {
                memberDto.id shouldBe member.id
                memberDto.email shouldBe member.email
                memberDto.accessToken shouldBe accessToken
                memberDto.lastLoginAt shouldBe member.lastLoginAt
            }
        }
    }
})
