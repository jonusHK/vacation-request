package com.bhkpo.vacation.domain

import com.bhkpo.vacation.ProjectConfig.Companion.createMemberPersist
import com.bhkpo.vacation.ProjectConfig.Companion.createVacationPersist
import com.bhkpo.vacation.ProjectConfig.Companion.validateVacationHistory
import com.bhkpo.vacation.adapter.inbound.presentation.PageInfo
import com.bhkpo.vacation.common.LocalDateTimeUtils.setMaxLocalTime
import com.bhkpo.vacation.common.LocalDateTimeUtils.setMinLocalTime
import com.bhkpo.vacation.common.WorkingHourProperties
import com.bhkpo.vacation.common.code.VacationHistoryStatus
import com.bhkpo.vacation.common.code.VacationHistoryType
import com.bhkpo.vacation.common.code.VacationType
import com.bhkpo.vacation.domain.auth.MemberEntity
import com.bhkpo.vacation.domain.vacation.VacationEntity
import com.bhkpo.vacation.domain.vacationhistory.VacationHistoryEntity
import com.bhkpo.vacation.domain.vacationhistory.VacationHistoryRepository
import com.bhkpo.vacation.domain.vacationhistory.VacationHistorySearchCondition
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import jakarta.persistence.EntityManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Transactional
internal class VacationHistoryRepositoryTest @Autowired constructor(
    private val em: EntityManager,
    private val vacationHistoryRepository: VacationHistoryRepository,
    private val workingHourProperties: WorkingHourProperties
) : BehaviorSpec({

    given("save") {
        val startAt = LocalDateTime.now()
        val member: MemberEntity = createMemberPersist(em)
        val vacation: VacationEntity = createVacationPersist(em, VacationType.FIXED, member)
        `when`("연차 휴가 내역을 생성하면") {
            then("데이터가 정상 생성된다.") {
                val days = 2L
                val endAt = LocalDateTime.now().plusDays(days)
                val comment = "코멘트"
                val vacationHistory: VacationHistoryEntity = VacationHistoryEntity.new(
                    vacation = vacation,
                    type = VacationHistoryType.DAY,
                    dayHalf = workingHourProperties.half,
                    dayQuarter = workingHourProperties.quarter,
                    startAt = startAt,
                    endAt = endAt,
                    days = days.toFloat(),
                    comment = comment
                )
                vacationHistoryRepository.save(vacationHistory)

                vacationHistory.id shouldNotBe null
                vacationHistory.vacation shouldBe vacation
                vacationHistory.vacation.member shouldBe member
                vacationHistory.startAt shouldBe startAt.setMinLocalTime()
                vacationHistory.endAt shouldBe endAt.setMaxLocalTime()
                vacationHistory.type shouldBe VacationHistoryType.DAY
                vacationHistory.days shouldBe days.toFloat()
                vacationHistory.comment shouldBe comment
                vacationHistory.status shouldBe VacationHistoryStatus.COMPLETED
            }
        }
        `when`("반차 휴가 내역을 생성하면") {
            then("데이터가 정상 생성된다.") {
                val comment = "코멘트"
                val vacationHistory: VacationHistoryEntity = VacationHistoryEntity.new(
                    vacation = vacation,
                    type = VacationHistoryType.HALF,
                    dayHalf = workingHourProperties.half,
                    dayQuarter = workingHourProperties.quarter,
                    startAt = startAt,
                    comment = comment
                )
                vacationHistoryRepository.save(vacationHistory)

                vacationHistory.id shouldNotBe null
                vacationHistory.validateVacationHistory(
                    startAt = startAt,
                    dayHalf = workingHourProperties.half,
                    dayQuarter = workingHourProperties.quarter,
                    vacation = vacation,
                    member = member,
                    comment = comment,
                    status = VacationHistoryStatus.COMPLETED
                )
            }
        }
        `when`("반반차 휴가 내역을 생성하면") {
            then("데이터가 정상 생성된다.") {
                val comment = "코멘트"
                val vacationHistory: VacationHistoryEntity = VacationHistoryEntity.new(
                    vacation = vacation,
                    type = VacationHistoryType.QUARTER,
                    dayHalf = workingHourProperties.half,
                    dayQuarter = workingHourProperties.quarter,
                    comment = comment,
                    startAt = startAt
                )
                vacationHistoryRepository.save(vacationHistory)

                with(vacationHistory) {
                    this.id shouldNotBe null
                    this.validateVacationHistory(
                        startAt = startAt,
                        dayHalf = workingHourProperties.half,
                        dayQuarter = workingHourProperties.quarter,
                        vacation = vacation,
                        member = member,
                        comment = comment,
                        status = VacationHistoryStatus.COMPLETED
                    )
                }
            }
        }
        `when`("휴가를 신청한 시작일시에 신청 내역이 존재한다면") {
            then("중복 여부가 확인된다.") {
                val days = 2L
                val endAt = startAt.plusDays(days)
                val existsVacationHistory = VacationHistoryEntity.new(
                    vacation = vacation,
                    type = VacationHistoryType.DAY,
                    dayHalf = workingHourProperties.half,
                    dayQuarter = workingHourProperties.quarter,
                    startAt = startAt,
                    endAt = endAt,
                    days = days.toFloat()
                )
                vacationHistoryRepository.save(existsVacationHistory)

                val vacationHistory = VacationHistoryEntity.new(
                    vacation = vacation,
                    type = VacationHistoryType.QUARTER,
                    dayHalf = workingHourProperties.half,
                    dayQuarter = workingHourProperties.quarter,
                    startAt = endAt.minusMinutes(1)
                )
                val condition = VacationHistorySearchCondition(
                    memberEmail = member.email,
                    vacationId = vacation.id,
                    status = VacationHistoryStatus.COMPLETED,
                    startAtLoe = vacationHistory.endAt,
                    endAtGoe = vacationHistory.startAt
                )

                vacationHistoryRepository.existsByCondition(condition) shouldBe true
            }
        }
        `when`("휴가를 신청한 종료일시에 신청 내역이 존재한다면") {
            then("중복 여부가 확인된다.") {
                val days = 2L
                val endAt = startAt.plusDays(days)
                val existsVacationHistory = VacationHistoryEntity.new(
                    vacation = vacation,
                    type = VacationHistoryType.DAY,
                    dayHalf = workingHourProperties.half,
                    dayQuarter = workingHourProperties.quarter,
                    startAt = startAt,
                    endAt = endAt,
                    days = days.toFloat()
                )
                vacationHistoryRepository.save(existsVacationHistory)

                val vacationHistory = VacationHistoryEntity.new(
                    vacation = vacation,
                    type = VacationHistoryType.QUARTER,
                    dayHalf = workingHourProperties.half,
                    dayQuarter = workingHourProperties.quarter,
                    startAt = startAt.minusMinutes(1)
                )
                val condition = VacationHistorySearchCondition(
                    memberEmail = member.email,
                    vacationId = vacation.id,
                    status = VacationHistoryStatus.COMPLETED,
                    startAtLoe = vacationHistory.endAt,
                    endAtGoe = vacationHistory.startAt
                )

                vacationHistoryRepository.existsByCondition(condition) shouldBe true
            }
        }
        `when`("휴가를 신청한 일시에 취소된 신청 내역이 존재한다면") {
            then("중복 여부가 확인되지 않는다.") {
                val days = 2L
                val endAt = startAt.plusDays(days)
                val existsVacationHistory = VacationHistoryEntity.new(
                    vacation = vacation,
                    type = VacationHistoryType.DAY,
                    dayHalf = workingHourProperties.half,
                    dayQuarter = workingHourProperties.quarter,
                    startAt = startAt,
                    endAt = endAt,
                    days = days.toFloat()
                )
                vacationHistoryRepository.save(existsVacationHistory)

                val vacationHistory = VacationHistoryEntity.new(
                    vacation = vacation,
                    type = VacationHistoryType.QUARTER,
                    dayHalf = workingHourProperties.half,
                    dayQuarter = workingHourProperties.quarter,
                    startAt = endAt.minusMinutes(1)
                )
                val condition = VacationHistorySearchCondition(
                    memberEmail = member.email,
                    vacationId = vacation.id,
                    status = VacationHistoryStatus.COMPLETED,
                    startAtLoe = vacationHistory.endAt,
                    endAtGoe = vacationHistory.startAt
                )

                vacationHistoryRepository.existsByCondition(condition) shouldBe true
            }
        }
    }

    given("findById") {
        val member: MemberEntity = createMemberPersist(em)
        val vacation: VacationEntity = createVacationPersist(em, VacationType.FIXED, member)
        val days = 5L
        val startAt = LocalDateTime.now()
        val endAt = startAt.plusDays(days)
        val comment = "코멘트"

        // 연차 신청 내역 생성
        val newVacationHistory: VacationHistoryEntity = VacationHistoryEntity.new(
            vacation = vacation,
            type = VacationHistoryType.DAY,
            dayHalf = workingHourProperties.half,
            dayQuarter = workingHourProperties.quarter,
            startAt = startAt,
            endAt = endAt,
            days = days.toFloat(),
            comment = comment
        )
        vacationHistoryRepository.save(newVacationHistory)

        `when`("특정 ID를 가진 휴가 내역을 조회하면") {
            val vacationHistory: VacationHistoryEntity? = vacationHistoryRepository.findById(newVacationHistory.id!!)

            then("데이터가 정상 조회된다.") {
                vacationHistory shouldNotBe null
                vacationHistory!!.id shouldBe newVacationHistory.id
                vacationHistory.validateVacationHistory(
                    startAt = startAt,
                    endAt = endAt,
                    days = days.toFloat(),
                    dayHalf = workingHourProperties.half,
                    dayQuarter = workingHourProperties.quarter,
                    vacation = vacation,
                    member = member,
                    comment = comment,
                    status = VacationHistoryStatus.COMPLETED
                )
            }
        }
    }

    given("countByCondition") {
        val member: MemberEntity = createMemberPersist(em)
        val vacation: VacationEntity = createVacationPersist(em, VacationType.FIXED, member)
        val days = 5L
        val startAt = LocalDateTime.now()
        val endAt = startAt.plusDays(days)

        // 동일한 회원으로 연차/반차/반반차 별 신청 내역 생성
        VacationHistoryType.values().forEach {
            vacationHistoryRepository.save(
                VacationHistoryEntity.new(
                    vacation = vacation,
                    type = it,
                    dayHalf = workingHourProperties.half,
                    dayQuarter = workingHourProperties.quarter,
                    startAt = startAt,
                    endAt = endAt,
                    days = days.toFloat()
                )
            )
        }

        `when`("type 을 조건으로 필터하면") {
            val condition = VacationHistorySearchCondition(
                memberEmail = member.email,
                type = VacationHistoryType.DAY
            )
            val vacationHistoryCnt: Long = vacationHistoryRepository.countByCondition(condition)
            then("필터된 총 데이터 수를 보여준다.") {
                vacationHistoryCnt shouldBe 1
            }
        }
        `when`("status 를 조건으로 필터하면") {
            val condition = VacationHistorySearchCondition(
                memberEmail = member.email,
                status = VacationHistoryStatus.COMPLETED
            )
            val vacationHistoryCnt: Long = vacationHistoryRepository.countByCondition(condition)
            then("필터된 총 데이터 수를 보여준다.") {
                vacationHistoryCnt shouldBe VacationHistoryType.values().size
            }
        }
    }

    given("searchByCondition") {
        val member: MemberEntity = createMemberPersist(em)
        val vacation: VacationEntity = createVacationPersist(em, VacationType.FIXED, member)
        val days = 5L
        val startAt = LocalDateTime.now()
        val endAt = startAt.plusDays(days)
        val offset = PageInfo.OFFSET
        val limit = PageInfo.LIMIT

        // 동일한 휴가 건에 대해 연차/반차/반반차 별 신청
        VacationHistoryType.values().forEach {
            vacationHistoryRepository.save(
                VacationHistoryEntity.new(
                    vacation = vacation,
                    type = it,
                    dayHalf = workingHourProperties.half,
                    dayQuarter = workingHourProperties.quarter,
                    startAt = startAt,
                    endAt = endAt,
                    days = days.toFloat()
                )
            )
        }
        // 다른 회원으로 휴가 생성 및 연차 신청
        val member2 = createMemberPersist(em, email = "test2@test.com", password = "test")
        val vacation2 = createVacationPersist(em, VacationType.FIXED, member2)
        vacationHistoryRepository.save(
            VacationHistoryEntity.new(
                vacation = vacation2,
                type = VacationHistoryType.DAY,
                dayHalf = workingHourProperties.half,
                dayQuarter = workingHourProperties.quarter,
                startAt = startAt,
                endAt = endAt,
                days = days.toFloat()
            )
        )

        `when`("vacationId 를 조건으로 필터하면") {
            val condition = VacationHistorySearchCondition(
                memberEmail = member.email,
                vacationId = vacation.id
            )
            val vacationHistories: List<VacationHistoryEntity>?
                = vacationHistoryRepository.searchByCondition(condition, offset, limit)
            then("데이터가 필터되어 조회된다.") {
                vacationHistories shouldNotBe null
                vacationHistories!!.size shouldBe VacationHistoryType.values().size
                vacationHistories.forEach {
                    it.vacation.id shouldBe vacation.id
                    it.vacation.member.id shouldBe member.id
                    it.validateVacationHistory(
                        startAt = startAt,
                        endAt = endAt,
                        days = days.toFloat(),
                        dayHalf = workingHourProperties.half,
                        dayQuarter = workingHourProperties.quarter
                    )
                }
            }
        }
        `when`("type 을 조건으로 필터하면") {
            val condition = VacationHistorySearchCondition(
                memberEmail = member.email,
                type = VacationHistoryType.DAY
            )
            val vacationHistories: List<VacationHistoryEntity>?
                    = vacationHistoryRepository.searchByCondition(condition, offset, limit)
            then("데이터가 필터되어 조회된다.") {
                vacationHistories shouldNotBe null
                vacationHistories!!.size shouldBe 1
                vacationHistories.forEach {
                    it.validateVacationHistory(
                        startAt = startAt,
                        endAt = endAt,
                        days = days.toFloat(),
                        dayHalf = workingHourProperties.half,
                        dayQuarter = workingHourProperties.quarter
                    )
                }
            }
        }
        `when`("status 를 조건으로 필터하면") {
            val condition = VacationHistorySearchCondition(
                memberEmail = member.email,
                status = VacationHistoryStatus.COMPLETED
            )
            val vacationHistories: List<VacationHistoryEntity>?
                    = vacationHistoryRepository.searchByCondition(condition, offset, limit)
            then("데이터가 필터되어 조회된다.") {
                vacationHistories shouldNotBe null
                vacationHistories!!.size shouldBe 3
                vacationHistories.forEach {
                    it.validateVacationHistory(
                        startAt = startAt,
                        endAt = endAt,
                        days = days.toFloat(),
                        dayHalf = workingHourProperties.half,
                        dayQuarter = workingHourProperties.quarter
                    )
                }

            }
        }
    }
})
