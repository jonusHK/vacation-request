package com.bhkpo.vacation.application

import com.bhkpo.vacation.ProjectConfig.Companion.createFixedVacation
import com.bhkpo.vacation.ProjectConfig.Companion.createMember
import com.bhkpo.vacation.adapter.inbound.presentation.PageInfo
import com.bhkpo.vacation.application.service.FixedVacationService
import com.bhkpo.vacation.common.code.VacationType
import com.bhkpo.vacation.common.dto.AuthenticationDto
import com.bhkpo.vacation.common.dto.FixedVacationDto
import com.bhkpo.vacation.domain.auth.MemberEntity
import com.bhkpo.vacation.domain.auth.MemberRepository
import com.bhkpo.vacation.domain.vacation.*
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.*
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
internal class FixedVacationServiceTest : DescribeSpec({

    afterContainer {
        clearAllMocks()
    }

    val memberRepository = mockk<MemberRepository>()
    val fixedVacationRepository = mockk<FixedVacationRepository>()
    val fixedVacationService: FixedVacationService = spyk(
        objToCopy = FixedVacationService(memberRepository, fixedVacationRepository),
        recordPrivateCalls = true
    )

    describe("create") {
        val targetYear = FixedVacationEntity.defaultTargetYear

        context("고정 휴가를 생성한 경우") {
            val member: MemberEntity = createMember()
            val memberId: Long = member.id!!
            val vacation: FixedVacationEntity = createFixedVacation(member)
            val condition = FixedVacationSearchCondition(
                memberEmail = member.email,
                targetYear = targetYear
            )

            every { memberRepository.findById(memberId) } returns member
            every { fixedVacationService["getSearchCondition"](member.email, targetYear) } returns condition
            every { fixedVacationRepository.existsByCondition(condition) } returns false
            every { fixedVacationService["generateEntity"](member, targetYear) } returns vacation
            every { fixedVacationRepository.save(vacation) } returns vacation

            // 고정 휴가 생성 로직 호출
            val fixedVacationDto: FixedVacationDto = fixedVacationService.create(
                memberId = memberId,
                targetYear = targetYear
            )

            it ("회원 ID를 조회하는지 확인한다.") {
                verify(exactly = 1) { memberRepository.findById(memberId) }
            }
            it("검색 조건 객체를 생성하는지 확인한다.") {
                verify(exactly = 1) { fixedVacationService["getSearchCondition"](member.email, targetYear) }
            }
            it("해당 연도로 휴가가 생성되어 있는지 확인한다.") {
                verify(exactly = 1) { fixedVacationRepository.existsByCondition(condition) }
            }
            it ("휴가 객체를 생성하는지 확인한다.") {
                verify(exactly = 1) { fixedVacationService["generateEntity"](member, targetYear) }
            }
            it("휴가 데이터를 한번만 생성하는지 확인한다.") {
                verify(exactly = 1) { fixedVacationRepository.save(vacation) }
            }
            it("데이터를 정상적으로 반환하는지 확인한다.") {
                fixedVacationDto.id shouldBe vacation.id
                fixedVacationDto.memberId shouldBe memberId
                fixedVacationDto.days shouldBe FixedVacationEntity.DEFAULT_DAYS
                fixedVacationDto.remainingDays shouldBe FixedVacationEntity.DEFAULT_DAYS
                fixedVacationDto.type shouldBe VacationType.FIXED
                fixedVacationDto.targetYear shouldBe FixedVacationEntity.defaultTargetYear
            }
        }
    }

    describe("search") {
        val offset = PageInfo.OFFSET
        val limit = PageInfo.LIMIT
        val targetYear = FixedVacationEntity.defaultTargetYear

        // 회원 객체 생성
        val member: MemberEntity = createMember()
        val authentication = AuthenticationDto(email = member.email, authorities = listOf())

        // 휴가 객체 생성
        val vacations = (1..5).map {
            createFixedVacation(member)
        }

        context("회원에게 부여된 휴가를 조회하는 경우") {
            val condition = FixedVacationSearchCondition(
                memberEmail = member.email,
                targetYear = targetYear
            )

            every {
                fixedVacationService["getSearchCondition"](member.email, targetYear)
            } returns condition
            every {
                fixedVacationRepository.searchByCondition(condition, offset, limit)
            } returns vacations

            // 회원에게 부여된 휴가 조회
            val result: List<FixedVacationDto> = fixedVacationService.search(
                authentication = authentication,
                targetYear = targetYear,
                offset = offset,
                limit = limit
            )

            it("검색 조건 객체를 생성하는지 확인한다.") {
                verify(exactly = 1) {
                    fixedVacationService["getSearchCondition"](member.email, targetYear)
                }
            }
            it("검색 조건 객체를 생성하는지 확인한다.") {
                verify(exactly = 1) {
                    fixedVacationRepository.searchByCondition(condition, offset, limit)
                }
            }
            it("변환된 데이터를 정상적으로 반환하는지 검증한다.") {
                result.forEach {
                    it.id shouldNotBe null
                    it.memberId shouldBe member.id
                    it.days shouldBe FixedVacationEntity.DEFAULT_DAYS
                    it.remainingDays shouldBe FixedVacationEntity.DEFAULT_DAYS
                    it.targetYear shouldBe targetYear
                }
            }
        }
    }
})
