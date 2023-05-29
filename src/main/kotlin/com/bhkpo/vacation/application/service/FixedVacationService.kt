package com.bhkpo.vacation.application.service

import com.bhkpo.vacation.application.port.inbound.vacation.CountFixedVacationsUseCase
import com.bhkpo.vacation.application.port.inbound.vacation.CreateFixedVacationUseCase
import com.bhkpo.vacation.application.port.inbound.vacation.FindFixedVacationByIdUseCase
import com.bhkpo.vacation.application.port.inbound.vacation.SearchFixedVacationsUseCase
import com.bhkpo.vacation.application.service.mapper.FixedVacationMapper
import com.bhkpo.vacation.common.dto.AuthenticationDto
import com.bhkpo.vacation.common.dto.FixedVacationDto
import com.bhkpo.vacation.common.exception.MemberAccessDeniedException
import com.bhkpo.vacation.common.exception.MemberNotExistException
import com.bhkpo.vacation.common.exception.VacationAlreadyCreatedException
import com.bhkpo.vacation.common.exception.VacationNotExistException
import com.bhkpo.vacation.domain.auth.MemberEntity
import com.bhkpo.vacation.domain.auth.MemberRepository
import com.bhkpo.vacation.domain.vacation.FixedVacationEntity
import com.bhkpo.vacation.domain.vacation.FixedVacationRepository
import com.bhkpo.vacation.domain.vacation.FixedVacationSearchCondition
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class FixedVacationService(
    private val memberRepository: MemberRepository,
    private val fixedVacationRepository: FixedVacationRepository
) : CreateFixedVacationUseCase, FindFixedVacationByIdUseCase, SearchFixedVacationsUseCase, CountFixedVacationsUseCase {

    /** 휴가 생성 */
    @Transactional
    override fun create(memberId: Long, targetYear: Int?): FixedVacationDto {

        val member: MemberEntity = memberRepository.findById(memberId)
            ?: throw MemberNotExistException()

        // 검색 조건 객체 생성
        val condition: FixedVacationSearchCondition = getSearchCondition(
            memberEmail = member.email,
            targetYear = targetYear ?: FixedVacationEntity.defaultTargetYear
        )

        // 해당 연도로 휴가가 생성되어 있는지 확인
        if (fixedVacationRepository.existsByCondition(condition)) {
            throw VacationAlreadyCreatedException()
        }

        // 휴가 생성
        val vacation: FixedVacationEntity = generateEntity(
            member = member,
            targetYear = targetYear
        )
        fixedVacationRepository.save(vacation)

        return FixedVacationMapper.toDto(vacation)
    }

    /** 특정 휴가 조회 */
    @Transactional(readOnly = true)
    override fun findById(
        authentication: AuthenticationDto,
        id: Long
    ): FixedVacationDto {

        // 휴가 조회
        val vacation: FixedVacationEntity = fixedVacationRepository.findById(id)
            ?: throw VacationNotExistException()

        // 해당 휴가에 대한 권한 검증
        if (authentication.email != vacation.member.email) {
            throw MemberAccessDeniedException()
        }

        return FixedVacationMapper.toDto(vacation)
    }

    /** 쿼리 총 갯수 조회 */
    @Transactional(readOnly = true)
    override fun count(
        authentication: AuthenticationDto,
        targetYear: Int?
    ): Long {

        // 검색 조건 객체 생성
        val condition = getSearchCondition(
            memberEmail = authentication.email,
            targetYear = targetYear
        )

        return fixedVacationRepository.countByCondition(condition)
    }

    /** 쿼리 조회 */
    @Transactional(readOnly = true)
    override fun search(
        authentication: AuthenticationDto,
        targetYear: Int?,
        offset: Long,
        limit: Long
    ): List<FixedVacationDto> {

        val condition: FixedVacationSearchCondition = getSearchCondition(
            memberEmail = authentication.email,
            targetYear = targetYear
        )
        val vacations: List<FixedVacationEntity>?
            = fixedVacationRepository.searchByCondition(condition, offset, limit)

        return vacations?.map { FixedVacationMapper.toDto(it) } ?: listOf()
    }

    private fun generateEntity(
        member: MemberEntity,
        targetYear: Int? = null
    ): FixedVacationEntity {

        return FixedVacationEntity.new(
            member = member,
            targetYear = targetYear
        )
    }

    /** 검색 조건 DTO 변환 */
    private fun getSearchCondition(
        memberEmail: String,
        targetYear: Int? = null
    ): FixedVacationSearchCondition {

        return FixedVacationSearchCondition(
            memberEmail = memberEmail,
            targetYear = targetYear
        )
    }
}
