package com.bhkpo.vacation.application.service

import com.bhkpo.vacation.application.port.inbound.vacationhistory.CancelVacationHistoryUseCase
import com.bhkpo.vacation.application.port.inbound.vacationhistory.CountVacationHistoriesUseCase
import com.bhkpo.vacation.application.port.inbound.vacationhistory.CreateVacationHistoryUseCase
import com.bhkpo.vacation.application.port.inbound.vacationhistory.SearchVacationHistoriesUseCase
import com.bhkpo.vacation.application.service.mapper.VacationHistoryMapper
import com.bhkpo.vacation.common.WorkingHourProperties
import com.bhkpo.vacation.common.code.VacationHistoryStatus
import com.bhkpo.vacation.common.code.VacationHistoryType
import com.bhkpo.vacation.common.dto.AuthenticationDto
import com.bhkpo.vacation.common.dto.CancelVacationHistoryDto
import com.bhkpo.vacation.common.dto.CreateVacationHistoryDto
import com.bhkpo.vacation.common.dto.VacationHistoryDto
import com.bhkpo.vacation.common.exception.*
import com.bhkpo.vacation.domain.vacation.VacationEntity
import com.bhkpo.vacation.domain.vacation.VacationRepository
import com.bhkpo.vacation.domain.vacationhistory.VacationHistoryEntity
import com.bhkpo.vacation.domain.vacationhistory.VacationHistoryRepository
import com.bhkpo.vacation.domain.vacationhistory.VacationHistorySearchCondition
import org.slf4j.LoggerFactory
import org.springframework.orm.ObjectOptimisticLockingFailureException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class VacationHistoryService(
    private val vacationRepository: VacationRepository,
    private val vacationHistoryRepository: VacationHistoryRepository,
    private val workingHourProperties: WorkingHourProperties
) : CreateVacationHistoryUseCase, CancelVacationHistoryUseCase, SearchVacationHistoriesUseCase, CountVacationHistoriesUseCase {

    private val log = LoggerFactory.getLogger(this.javaClass)

    /** 휴가 신청 */
    @Transactional
    override fun create(
        authentication: AuthenticationDto,
        vacationId: Long,
        type: VacationHistoryType,
        startAt: LocalDateTime,
        endAt: LocalDateTime?,
        days: Float?,
        comment: String?
    ): CreateVacationHistoryDto {

        // 휴가 조회
        val vacation: VacationEntity = vacationRepository.findById(vacationId)
            ?: throw VacationNotExistException()

        // 휴가 신청 권한이 있는지 확인
        if (vacation.member.email != authentication.email) {
            throw MemberAccessDeniedException()
        }

        // 휴가 신청 객체 생성
        val vacationHistory: VacationHistoryEntity = generateEntity(
            vacation = vacation,
            type = type,
            dayHalf = workingHourProperties.half,
            dayQuarter = workingHourProperties.quarter,
            startAt = startAt,
            endAt = endAt,
            days = days,
            comment = comment
        )

        // 중복 휴가 신청 기간 여부 확인
        val condition: VacationHistorySearchCondition = getSearchCondition(
            memberEmail = authentication.email,
            vacationId = vacationId,
            status = vacationHistory.status,
            startAtLoe = vacationHistory.endAt,
            endAtGoe = vacationHistory.startAt
        )
        if (vacationHistoryRepository.existsByCondition(condition)) {
            throw VacationHistoryDuplicatedPeriodException()
        }

        try {
            vacationHistoryRepository.saveAndFlush(vacationHistory)
        } catch (e: ObjectOptimisticLockingFailureException) {
            log.error("휴가 신청 시 동시성 예외 발생 vacationId={}", vacationId)
            throw VacationHistoryConcurrencyException()
        } catch (e: Throwable) {
            log.error("휴가 신청 예외 발생 exception={}", e.message)
            throw e
        }

        return VacationHistoryMapper.toCreateDto(vacationHistory)
    }

    /** 휴가 신청 취소 */
    @Transactional
    override fun cancel(authentication: AuthenticationDto, id: Long): CancelVacationHistoryDto {

        val vacationHistory: VacationHistoryEntity = vacationHistoryRepository.findById(id)
            ?: throw VacationHistoryNotExistException()

        // 다른 회원의 휴가를 취소하는 경우
        if (vacationHistory.vacation.member.email != authentication.email) {
            throw throw MemberAccessDeniedException()
        }

        // 휴가 신청 건 취소
        vacationHistory.cancel()
        return VacationHistoryMapper.toCancelDto(vacationHistory)
    }

    /** 휴가 신청 내역 쿼리 총 갯수 조회 */
    @Transactional(readOnly = true)
    override fun count(
        authentication: AuthenticationDto,
        vacationId: Long?,
        type: VacationHistoryType?,
        status: VacationHistoryStatus?,
        startAtLoe: LocalDateTime?,
        startAtGoe: LocalDateTime?,
        endAtLoe: LocalDateTime?,
        endAtGoe: LocalDateTime?
    ): Long {

        val condition: VacationHistorySearchCondition = getSearchCondition(
            memberEmail = authentication.email,
            vacationId = vacationId,
            type = type,
            status = status,
            startAtLoe = startAtLoe,
            startAtGoe = startAtGoe,
            endAtLoe = endAtLoe,
            endAtGoe = endAtGoe
        )

        return vacationHistoryRepository.countByCondition(condition)
    }

    /** 휴가 신청 내역 쿼리 조회 */
    @Transactional(readOnly = true)
    override fun search(
        authentication: AuthenticationDto,
        vacationId: Long?,
        type: VacationHistoryType?,
        status: VacationHistoryStatus?,
        startAtLoe: LocalDateTime?,
        startAtGoe: LocalDateTime?,
        endAtLoe: LocalDateTime?,
        endAtGoe: LocalDateTime?,
        offset: Long,
        limit: Long
    ): List<VacationHistoryDto> {

        val condition: VacationHistorySearchCondition = getSearchCondition(
            memberEmail = authentication.email,
            vacationId = vacationId,
            type = type,
            status = status,
            startAtLoe = startAtLoe,
            startAtGoe = startAtGoe,
            endAtLoe = endAtLoe,
            endAtGoe = endAtGoe,
        )
        val vacationHistories : List<VacationHistoryEntity>?
            = vacationHistoryRepository.searchByCondition(condition, offset, limit)

        return vacationHistories?.map { VacationHistoryMapper.toDto(it) } ?: listOf()
    }

    /** 엔티티 객체 생성 */
    private fun generateEntity(
        vacation: VacationEntity,
        type: VacationHistoryType,
        dayHalf: Long,
        dayQuarter: Long,
        startAt: LocalDateTime,
        endAt: LocalDateTime? = null,
        days: Float? = null,
        comment: String? = null
    ): VacationHistoryEntity {

        return VacationHistoryEntity.new(
            vacation = vacation,
            type = type,
            dayHalf = dayHalf,
            dayQuarter = dayQuarter,
            startAt = startAt,
            endAt = endAt,
            days = days,
            comment = comment
        )
    }

    /** 검색 조건 DTO 변환 */
    private fun getSearchCondition(
        memberEmail: String,
        vacationId: Long? = null,
        type: VacationHistoryType? = null,
        status: VacationHistoryStatus? = null,
        startAtLoe: LocalDateTime? = null,
        startAtGoe: LocalDateTime? = null,
        endAtLoe: LocalDateTime? = null,
        endAtGoe: LocalDateTime? = null,
    ): VacationHistorySearchCondition {

        return VacationHistorySearchCondition(
            memberEmail = memberEmail,
            vacationId = vacationId,
            type = type,
            status = status,
            startAtLoe = startAtLoe,
            startAtGoe = startAtGoe,
            endAtLoe = endAtLoe,
            endAtGoe = endAtGoe
        )
    }
}
