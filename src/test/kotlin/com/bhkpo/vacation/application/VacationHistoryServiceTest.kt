package com.bhkpo.vacation.application

import com.bhkpo.vacation.ProjectConfig.Companion.createMember
import com.bhkpo.vacation.ProjectConfig.Companion.createVacation
import com.bhkpo.vacation.ProjectConfig.Companion.createVacationHistory
import com.bhkpo.vacation.adapter.inbound.presentation.PageInfo
import com.bhkpo.vacation.application.service.VacationHistoryService
import com.bhkpo.vacation.common.LocalDateTimeUtils.setMaxLocalTime
import com.bhkpo.vacation.common.LocalDateTimeUtils.setMinLocalTime
import com.bhkpo.vacation.common.LocalDateTimeUtils.setUntilMinutes
import com.bhkpo.vacation.common.WorkingHourProperties
import com.bhkpo.vacation.common.code.VacationHistoryStatus
import com.bhkpo.vacation.common.code.VacationHistoryType
import com.bhkpo.vacation.common.dto.AuthenticationDto
import com.bhkpo.vacation.common.dto.CreateVacationHistoryDto
import com.bhkpo.vacation.common.dto.VacationHistoryDto
import com.bhkpo.vacation.common.exception.VacationHistoryAlreadyCanceledException
import com.bhkpo.vacation.common.exception.VacationHistoryAlreadyStartedException
import com.bhkpo.vacation.common.exception.VacationHistoryDuplicatedPeriodException
import com.bhkpo.vacation.common.exception.VacationRemainingDaysLackException
import com.bhkpo.vacation.domain.auth.MemberEntity
import com.bhkpo.vacation.domain.vacation.FixedVacationEntity
import com.bhkpo.vacation.domain.vacation.VacationEntity
import com.bhkpo.vacation.domain.vacation.VacationRepository
import com.bhkpo.vacation.domain.vacationhistory.VacationHistoryEntity
import com.bhkpo.vacation.domain.vacationhistory.VacationHistoryRepository
import com.bhkpo.vacation.domain.vacationhistory.VacationHistorySearchCondition
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.*
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.extension.ExtendWith
import java.time.LocalDateTime
import java.util.*

@ExtendWith(MockKExtension::class)
internal class VacationHistoryServiceTest : DescribeSpec({

    afterContainer {
        clearAllMocks()
    }

    val vacationRepository = mockk<VacationRepository>()
    val vacationHistoryRepository = mockk<VacationHistoryRepository>()
    val workingHourProperties = mockk<WorkingHourProperties>()
    val vacationHistoryService: VacationHistoryService = spyk(
        objToCopy = VacationHistoryService(
            vacationRepository,
            vacationHistoryRepository,
            workingHourProperties
        ),
        recordPrivateCalls = true
    )

    val offset = PageInfo.OFFSET
    val limit = PageInfo.LIMIT
    val dayHalf = 4L
    val dayQuarter = 2L
    val comment = "코멘트"
    val now = LocalDateTime.now()

    describe("create") {
        context("연차 휴가 신청한 경우") {
            val type = VacationHistoryType.DAY
            val days = 5F
            val startAt = now.plusDays(1)
            val endAt = startAt.plusDays(days.toLong())

            val member: MemberEntity = createMember()
            val authentication = AuthenticationDto(email = member.email)

            val vacation: VacationEntity = createVacation(member)
            val vacationId: Long = vacation.id!!

            val vacationHistory: VacationHistoryEntity = createVacationHistory(
                vacation = vacation,
                type = type,
                dayHalf = dayHalf,
                dayQuarter = dayQuarter,
                startAt = startAt,
                endAt = endAt,
                days = days,
                comment = comment
            )
            val condition = VacationHistorySearchCondition(
                memberEmail = authentication.email,
                vacationId = vacationId,
                status = vacationHistory.status,
                startAtLoe = vacationHistory.endAt,
                endAtGoe = vacationHistory.startAt
            )

            every { vacationRepository.findById(vacationId) } returns vacation
            every { workingHourProperties.half } returns dayHalf
            every { workingHourProperties.quarter } returns dayQuarter
            every {
                vacationHistoryService["generateEntity"](
                    vacation, type, dayHalf, dayQuarter, startAt, endAt, days, comment
                )
            } returns vacationHistory
            every {
                vacationHistoryService["getSearchCondition"](
                    authentication.email,
                    vacationId,
                    vacationHistory.type,
                    vacationHistory.status,
                    vacationHistory.endAt,
                    LocalDateTime.MIN,
                    LocalDateTime.MAX,
                    vacationHistory.startAt
                )
            }
            every { vacationHistoryRepository.existsByCondition(condition) } returns false
            every { vacationHistoryRepository.save(vacationHistory) } returns vacationHistory
            every { vacationHistoryRepository.saveAndFlush(vacationHistory) } returns vacationHistory

            val vacationHistoryDto: CreateVacationHistoryDto = vacationHistoryService.create(
                authentication = authentication,
                vacationId = vacationId,
                type = type,
                startAt = startAt,
                endAt = endAt,
                days = days,
                comment = comment
            )

            it("휴가 잔여일수가 차감되는지 확인한다.") {
                vacationHistory.days shouldBe vacation.days - vacation.remainingDays
            }
            it("휴가 신청 유형이 반차로 등록되었는지 확인한다.") {
                vacationHistory.type shouldBe VacationHistoryType.DAY
            }
            it("휴가 신청 상태가 완료로 등록되었는지 확인한다.") {
                vacationHistory.status shouldBe VacationHistoryStatus.COMPLETED
            }
            it("휴가 신청 코멘트가 등록되었는지 확인한다.") {
                vacationHistory.comment shouldBe comment
            }
            it("휴가 신청 기간이 정상적으로 등록되었는지 확인한다.") {
                vacationHistory.startAt shouldBe startAt.setMinLocalTime()
                vacationHistory.endAt shouldBe startAt.plusDays(days.toLong()).setMaxLocalTime()
                vacationHistory.days shouldBe days
            }
            it("휴가 신청 ID와 잔여 일수 값을 반환하는지 확인한다.") {
                vacationHistoryDto.id shouldBe vacationHistory.id
                vacationHistoryDto.remainingDays shouldBe vacation.remainingDays
            }
        }
        context("반차 휴가 신청한 경우") {
            val type = VacationHistoryType.HALF
            val startAt = now.plusHours(1)
            val endAt = startAt.plusHours(dayHalf)
            val days = VacationHistoryType.HALF.value

            val member: MemberEntity = createMember()
            val authentication = AuthenticationDto(email = member.email)

            val vacation: VacationEntity = createVacation(member)
            val vacationId: Long = vacation.id!!

            val vacationHistory: VacationHistoryEntity = createVacationHistory(
                vacation = vacation,
                type = type,
                dayHalf = dayHalf,
                dayQuarter = dayQuarter,
                startAt = startAt,
                comment = comment
            )
            val condition = VacationHistorySearchCondition(
                memberEmail = authentication.email,
                vacationId = vacationId,
                status = vacationHistory.status,
                startAtLoe = vacationHistory.endAt,
                endAtGoe = vacationHistory.startAt
            )

            every { vacationRepository.findById(vacationId) } returns vacation
            every { workingHourProperties.half } returns dayHalf
            every { workingHourProperties.quarter } returns dayQuarter
            every {
                vacationHistoryService["generateEntity"](
                    vacation, type, dayHalf, dayQuarter, startAt, endAt, days, comment
                )
            } returns vacationHistory
            every {
                vacationHistoryService["getSearchCondition"](
                    authentication.email,
                    vacationId,
                    vacationHistory.type,
                    vacationHistory.status,
                    vacationHistory.endAt,
                    LocalDateTime.MIN,
                    LocalDateTime.MAX,
                    vacationHistory.startAt
                )
            }
            every { vacationHistoryRepository.existsByCondition(condition) } returns false
            every { vacationHistoryRepository.save(vacationHistory) } returns vacationHistory
            every { vacationHistoryRepository.saveAndFlush(vacationHistory) } returns vacationHistory
            every { vacationHistoryRepository.saveAndFlush(vacationHistory) } returns vacationHistory
            every { vacationHistoryRepository.saveAndFlush(vacationHistory) } returns vacationHistory

            val vacationHistoryDto: CreateVacationHistoryDto = vacationHistoryService.create(
                authentication = authentication,
                vacationId = vacationId,
                type = type,
                startAt = startAt,
                endAt = endAt,
                days = days,
                comment = comment
            )

            it("휴가 잔여일수가 차감되는지 확인한다.") {
                vacationHistory.days shouldBe vacation.days - vacation.remainingDays
            }
            it("휴가 신청 유형이 반차로 등록되었는지 확인한다.") {
                vacationHistory.type shouldBe VacationHistoryType.HALF
            }
            it("휴가 신청 상태가 완료로 등록되었는지 확인한다.") {
                vacationHistory.status shouldBe VacationHistoryStatus.COMPLETED
            }
            it("휴가 신청 코멘트가 등록되었는지 확인한다.") {
                vacationHistory.comment shouldBe comment
            }
            it("휴가 신청 기간이 정상적으로 등록되었는지 확인한다.") {
                vacationHistory.startAt shouldBe startAt.setUntilMinutes()
                vacationHistory.endAt shouldBe startAt.plusHours(dayHalf).setUntilMinutes()
                vacationHistory.days shouldBe VacationHistoryType.HALF.value
            }
            it("휴가 신청 ID와 잔여 일수 값을 반환하는지 확인한다.") {
                vacationHistoryDto.id shouldBe vacationHistory.id
                vacationHistoryDto.remainingDays shouldBe vacation.remainingDays
            }
        }
        context("반반차 휴가 신청한 경우") {
            val type = VacationHistoryType.QUARTER
            val startAt = now.plusHours(1)
            val endAt = startAt.plusHours(dayQuarter)
            val days = VacationHistoryType.QUARTER.value

            val member: MemberEntity = createMember()
            val authentication = AuthenticationDto(email = member.email)

            val vacation: VacationEntity = createVacation(member)
            val vacationId: Long = vacation.id!!

            val vacationHistory: VacationHistoryEntity = createVacationHistory(
                vacation = vacation,
                type = type,
                dayHalf = dayHalf,
                dayQuarter = dayQuarter,
                startAt = startAt,
                comment = comment
            )
            val condition = VacationHistorySearchCondition(
                memberEmail = authentication.email,
                vacationId = vacationId,
                status = vacationHistory.status,
                startAtLoe = vacationHistory.endAt,
                endAtGoe = vacationHistory.startAt
            )

            every { vacationRepository.findById(vacationId) } returns vacation
            every { workingHourProperties.half } returns dayHalf
            every { workingHourProperties.quarter } returns dayQuarter
            every {
                vacationHistoryService["generateEntity"](
                    vacation, type, dayHalf, dayQuarter, startAt, endAt, days, comment
                )
            } returns vacationHistory
            every {
                vacationHistoryService["getSearchCondition"](
                    authentication.email,
                    vacationId,
                    vacationHistory.type,
                    vacationHistory.status,
                    vacationHistory.endAt,
                    LocalDateTime.MIN,
                    LocalDateTime.MAX,
                    vacationHistory.startAt
                )
            }
            every { vacationHistoryRepository.existsByCondition(condition) } returns false
            every { vacationHistoryRepository.save(vacationHistory) } returns vacationHistory
            every { vacationHistoryRepository.saveAndFlush(vacationHistory) } returns vacationHistory
            every { vacationHistoryRepository.saveAndFlush(vacationHistory) } returns vacationHistory

            val vacationHistoryDto: CreateVacationHistoryDto = vacationHistoryService.create(
                authentication = authentication,
                vacationId = vacationId,
                type = type,
                startAt = startAt,
                endAt = endAt,
                days = days,
                comment = comment
            )

            it("휴가 잔여일수가 차감되는지 확인한다.") {
                vacationHistory.days shouldBe vacation.days - vacation.remainingDays
            }
            it("휴가 신청 유형이 반반차로 등록되었는지 확인한다.") {
                vacationHistory.type shouldBe VacationHistoryType.QUARTER
            }
            it("휴가 신청 상태가 완료로 등록되었는지 확인한다.") {
                vacationHistory.status shouldBe VacationHistoryStatus.COMPLETED
            }
            it("휴가 신청 코멘트가 등록되었는지 확인한다.") {
                vacationHistory.comment shouldBe comment
            }
            it("휴가 신청 기간이 정상적으로 등록되었는지 확인한다.") {
                vacationHistory.startAt shouldBe startAt.setUntilMinutes()
                vacationHistory.endAt shouldBe startAt.plusHours(dayQuarter).setUntilMinutes()
                vacationHistory.days shouldBe VacationHistoryType.QUARTER.value
            }
            it("휴가 신청 ID와 잔여 일수 값을 반환하는지 확인한다.") {
                vacationHistoryDto.id shouldBe vacationHistory.id
                vacationHistoryDto.remainingDays shouldBe vacation.remainingDays
            }
        }
        context("중복되는 휴가 기간이 있는 경우") {
            val type = VacationHistoryType.DAY
            val days = 5F
            val startAt = now.plusDays(1)
            val endAt = startAt.plusDays(days.toLong())

            val member: MemberEntity = createMember()
            val authentication = AuthenticationDto(email = member.email)

            val vacation: VacationEntity = createVacation(member)
            val vacationId: Long = vacation.id!!

            val vacationHistory: VacationHistoryEntity = createVacationHistory(
                vacation = vacation,
                type = type,
                dayHalf = dayHalf,
                dayQuarter = dayQuarter,
                startAt = startAt,
                endAt = endAt,
                days = days,
                comment = comment
            )
            val condition = VacationHistorySearchCondition(
                memberEmail = authentication.email,
                vacationId = vacationId,
                status = vacationHistory.status,
                startAtLoe = vacationHistory.endAt,
                endAtGoe = vacationHistory.startAt
            )

            every { vacationRepository.findById(vacationId) } returns vacation
            every { workingHourProperties.half } returns dayHalf
            every { workingHourProperties.quarter } returns dayQuarter
            every {
                vacationHistoryService["generateEntity"](
                    vacation, type, dayHalf, dayQuarter, startAt, endAt, days, comment
                )
            } returns vacationHistory
            every {
                vacationHistoryService["getSearchCondition"](
                    authentication.email,
                    vacationId,
                    vacationHistory.type,
                    vacationHistory.status,
                    vacationHistory.endAt,
                    LocalDateTime.MIN,
                    LocalDateTime.MAX,
                    vacationHistory.startAt
                )
            }
            every { vacationHistoryRepository.existsByCondition(condition) } returns true
            every { vacationHistoryRepository.save(vacationHistory) } returns vacationHistory
            every { vacationHistoryRepository.saveAndFlush(vacationHistory) } returns vacationHistory

            shouldThrow<VacationHistoryDuplicatedPeriodException> {
                vacationHistoryService.create(
                    authentication = authentication,
                    vacationId = vacationId,
                    type = type,
                    startAt = startAt,
                    endAt = endAt,
                    days = days,
                    comment = comment
                )
            }
        }
        context("휴가 잔여일수가 부족한 경우") {
            val type = VacationHistoryType.DAY
            val days = FixedVacationEntity.DEFAULT_DAYS + 1
            val startAt = now.plusDays(1)
            val endAt = startAt.plusDays(days.toLong())

            val member: MemberEntity = createMember()
            val vacation: VacationEntity = createVacation(member)

            it("예외가 발생하는지 확인한다.") {
                shouldThrow<VacationRemainingDaysLackException> {
                    createVacationHistory(
                        vacation = vacation,
                        type = type,
                        dayHalf = dayHalf,
                        dayQuarter = dayQuarter,
                        startAt = startAt,
                        endAt = endAt,
                        days = days
                    )
                }
            }
        }
    }

    describe("cancel") {
        context("연차 신청을 취소하는 경우") {
            val type = VacationHistoryType.DAY
            val days = 5L
            val startAt = now.plusDays(1)
            val endAt = startAt.plusDays(days)

            val member: MemberEntity = createMember()
            val authentication = AuthenticationDto(email = member.email)

            val vacation: VacationEntity = createVacation(member)
            val vacationHistory: VacationHistoryEntity = createVacationHistory(
                vacation = vacation,
                type = type,
                dayHalf = dayHalf,
                dayQuarter = dayQuarter,
                startAt = startAt,
                endAt = endAt,
                days = days.toFloat()
            )
            val vacationHistoryId: Long = vacationHistory.id!!

            // 휴가 신청 후 잔여일수
            val initialRemainingDays: Float = vacation.remainingDays

            every {
                vacationHistoryRepository.findById(vacationHistoryId)
            } returns vacationHistory

            // 휴가 신청 취소
            vacationHistoryService.cancel(authentication, vacationHistoryId)

            it("휴가 신청 내역을 조회하는지 확인한다.") {
                verify(atLeast = 1) { vacationHistoryRepository.findById(vacationHistoryId) }
            }
            it("연차 취소한 일수만큼 휴가 일수가 증가하는지 확인한다.") {
                vacationHistory.days shouldBe (vacation.remainingDays - initialRemainingDays)
            }
            it("휴가 신청 내역이 취소되었는지 확인한다.") {
                vacationHistory.status shouldBe VacationHistoryStatus.CANCELED
            }
            it("휴가 신청 일시가 저장되었는지 확인한다.") {
                vacationHistory.canceledAt shouldNotBe null
            }
        }
        context("반차 신청을 취소하는 경우") {
            val type = VacationHistoryType.HALF
            val startAt = now.plusHours(1)

            val member: MemberEntity = createMember()
            val authentication = AuthenticationDto(email = member.email)

            val vacation: VacationEntity = createVacation(member)
            val vacationHistory: VacationHistoryEntity = createVacationHistory(
                vacation = vacation,
                type = type,
                dayHalf = dayHalf,
                dayQuarter = dayQuarter,
                startAt = startAt
            )
            val vacationHistoryId: Long = vacationHistory.id!!

            // 휴가 신청 이후 초기 잔여 일수
            val initialRemainingDays: Float = vacation.remainingDays

            every {
                vacationHistoryRepository.findById(vacationHistoryId)
            } returns vacationHistory

            // 휴가 신청 취소
            vacationHistoryService.cancel(authentication, vacationHistoryId)

            it("반차 취소한 일수만큼 휴가 일수가 증가하는지 확인한다.") {
                vacationHistory.days shouldBe vacation.remainingDays - initialRemainingDays
            }
            it("휴가 신청 내역이 취소되었는지 확인한다.") {
                vacationHistory.status shouldBe VacationHistoryStatus.CANCELED
            }
            it("휴가 신청 일시가 저장되었는지 확인한다.") {
                vacationHistory.canceledAt shouldNotBe null
            }
        }
        context("반반차 신청을 취소하는 경우") {
            val type = VacationHistoryType.QUARTER
            val startAt = now.plusHours(1)

            val member: MemberEntity = createMember()
            val authentication = AuthenticationDto(email = member.email)

            val vacation: VacationEntity = createVacation(member)
            val vacationHistory: VacationHistoryEntity = createVacationHistory(
                vacation = vacation,
                type = type,
                dayHalf = dayHalf,
                dayQuarter = dayQuarter,
                startAt = startAt
            )

            // 휴가 신청 이후 초기 잔여일수
            val initialRemainingDays: Float = vacation.remainingDays

            every {
                vacationHistoryRepository.findById(vacationHistory.id!!)
            } returns vacationHistory

            // 휴가 신청 취소
            vacationHistoryService.cancel(authentication, vacationHistory.id!!)

            it("반반차 취소한 일수만큼 휴가 일수가 증가하는지 확인한다.") {
                vacationHistory.days shouldBe vacation.remainingDays - initialRemainingDays
            }
            it("휴가 신청 내역이 취소되었는지 확인한다.") {
                vacationHistory.status shouldBe VacationHistoryStatus.CANCELED
            }
            it("휴가 신청 일시가 저장되었는지 확인한다.") {
                vacationHistory.canceledAt shouldNotBe null
            }
        }

        context("휴가 시작 일시 이후에 취소하는 경우") {
            val type = VacationHistoryType.QUARTER
            val startAt = now.minusHours(1)

            val member: MemberEntity = createMember()
            val authentication = AuthenticationDto(email = member.email)

            val vacation: VacationEntity = createVacation(member)
            val vacationHistory: VacationHistoryEntity = createVacationHistory(
                vacation = vacation,
                type = type,
                dayHalf = dayHalf,
                dayQuarter = dayQuarter,
                startAt = startAt
            )

            every {
                vacationHistoryRepository.findById(vacationHistory.id!!)
            } returns vacationHistory

            it("예외 발생하는지 확인한다.") {
                shouldThrow<VacationHistoryAlreadyStartedException> {
                    // 휴가 신청 취소
                    vacationHistoryService.cancel(authentication, vacationHistory.id!!)
                }
            }
        }
        context("이미 취소된 휴가인 경우") {
            val type = VacationHistoryType.QUARTER
            val startAt = now.plusHours(1)

            val member: MemberEntity = createMember()
            val authentication = AuthenticationDto(email = member.email)

            val vacation: VacationEntity = createVacation(member)
            val vacationHistory: VacationHistoryEntity = createVacationHistory(
                vacation = vacation,
                type = type,
                dayHalf = dayHalf,
                dayQuarter = dayQuarter,
                startAt = startAt
            ).apply { this.status = VacationHistoryStatus.CANCELED }

            every {
                vacationHistoryRepository.findById(vacationHistory.id!!)
            } returns vacationHistory

            it("예외 발생하는지 확인한다.") {
                shouldThrow<VacationHistoryAlreadyCanceledException> {
                    // 휴가 신청 취소
                    vacationHistoryService.cancel(authentication, vacationHistory.id!!)
                }
            }
        }
    }

    describe("search") {
        context("특정 휴가에 대한 신청 내역을 조회하는 경우") {
            val days = 5L
            val startAt = now.plusDays(1)
            val endAt = startAt.plusDays(days)

            val member: MemberEntity = createMember()
            val authentication = AuthenticationDto(email = member.email)

            val vacation: VacationEntity = createVacation(member)
            val vacationHistories: List<VacationHistoryEntity> = VacationHistoryType.values().map {
                createVacationHistory(
                    vacation = vacation,
                    type = it,
                    dayHalf = dayHalf,
                    dayQuarter = dayQuarter,
                    startAt = startAt,
                    endAt = endAt,
                    days = days.toFloat(),
                    comment = comment
                )
            }
            val condition = VacationHistorySearchCondition(
                memberEmail = member.email,
                vacationId = vacation.id!!
            )

            val fakeType = VacationHistoryType.DAY
            val fakeStatus = VacationHistoryStatus.COMPLETED
            val fakeStartAtLoe = LocalDateTime.MAX
            val fakeStartAtGoe = LocalDateTime.MIN
            val fakeEndAtLoe = LocalDateTime.MAX
            val fakeEndAtGoe = LocalDateTime.MIN

            every {
                vacationHistoryService["getSearchCondition"](
                    authentication.email,
                    vacation.id!!,
                    fakeType,
                    fakeStatus,
                    fakeStartAtLoe,
                    fakeStartAtGoe,
                    fakeEndAtLoe,
                    fakeEndAtGoe
                )
            } returns condition
            every {
                vacationHistoryRepository.searchByCondition(condition, offset, limit)
            } returns vacationHistories

            // 회원에게 부여된 휴가 조회
            val result: List<VacationHistoryDto> = vacationHistoryService.search(
                authentication = authentication,
                vacationId = vacation.id!!,
                type = fakeType,
                status = fakeStatus,
                startAtLoe = fakeStartAtLoe,
                startAtGoe = fakeStartAtGoe,
                endAtLoe = fakeEndAtLoe,
                endAtGoe = fakeEndAtGoe,
                offset = offset,
                limit = limit
            )

            it("검색 조건으로 쿼리를 가져오는지 확인한다.") {
                verify(exactly = 1) {
                    vacationHistoryRepository.searchByCondition(condition, offset, limit)
                }
            }
            it("변환된 데이터를 정상적으로 반환하는지 검증한다.") {
                result.forEach {
                    it.id shouldNotBe null
                    it.vacationId shouldBe vacation.id!!
                    it.status shouldBe VacationHistoryStatus.COMPLETED
                    it.canceledAt shouldBe null
                    it.comment shouldBe comment
                    when (it.type) {
                        VacationHistoryType.DAY -> {
                            it.startAt shouldBe startAt.setMinLocalTime()
                            it.endAt shouldBe endAt.setMaxLocalTime()
                            it.days shouldBe days.toFloat()
                        }
                        VacationHistoryType.HALF -> {
                            it.startAt shouldBe startAt.setUntilMinutes()
                            it.endAt shouldBe startAt.plusHours(dayHalf).setUntilMinutes()
                            it.days shouldBe VacationHistoryType.HALF.value
                        }
                        VacationHistoryType.QUARTER -> {
                            it.startAt shouldBe startAt.setUntilMinutes()
                            it.endAt shouldBe startAt.plusHours(dayQuarter).setUntilMinutes()
                            it.days shouldBe VacationHistoryType.QUARTER.value
                        }
                    }
                }
            }
        }
    }
})
