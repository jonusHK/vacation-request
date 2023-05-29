package com.bhkpo.vacation.adapter.inbound.presentation

import com.bhkpo.vacation.adapter.inbound.presentation.dto.CreateVacationDto
import com.bhkpo.vacation.adapter.inbound.presentation.dto.request.SearchFixedVacationDto
import com.bhkpo.vacation.adapter.inbound.presentation.dto.response.CollectionResponseDto
import com.bhkpo.vacation.application.port.inbound.auth.ExtractAuthenticationUseCase
import com.bhkpo.vacation.application.port.inbound.vacation.CountFixedVacationsUseCase
import com.bhkpo.vacation.application.port.inbound.vacation.CreateFixedVacationUseCase
import com.bhkpo.vacation.application.port.inbound.vacation.FindFixedVacationByIdUseCase
import com.bhkpo.vacation.application.port.inbound.vacation.SearchFixedVacationsUseCase
import com.bhkpo.vacation.common.dto.AuthenticationDto
import com.bhkpo.vacation.common.dto.FixedVacationDto
import com.bhkpo.vacation.common.exception.AuthenticationNotExistException
import org.springframework.http.HttpStatus
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/vacation")
class VacationController(
    private val countFixedVacationsUseCase: CountFixedVacationsUseCase,
    private val searchFixedVacationsUseCase: SearchFixedVacationsUseCase,
    private val createFixedVacationUseCase: CreateFixedVacationUseCase,
    private val findFixedVacationByIdUseCase: FindFixedVacationByIdUseCase,
    private val extractAuthenticationUseCase: ExtractAuthenticationUseCase
) {
    /**
     * 고정 휴가 리스트 조회
     *
     * @param: targetYear?, offset?, limit?
     * @return total, items, offset, limit
     */
    @GetMapping("/search/fixed")
    fun searchFixed(@Validated data: SearchFixedVacationDto): CollectionResponseDto<FixedVacationDto> {

        // 인증 객체 조회
        val authentication: AuthenticationDto = extractAuthenticationUseCase.extractAuthentication()
            ?: throw AuthenticationNotExistException()

        // 검색된 총 결과 수 조회
        val total: Long = countFixedVacationsUseCase.count(authentication)

        // 검색 조건에 해당하는 고정 휴가 목록 조회
        val vacations: List<FixedVacationDto> = searchFixedVacationsUseCase.search(
            authentication = authentication,
            targetYear = data.targetYear,
            offset = data.offset,
            limit = data.limit
        )

        return CollectionResponseDto(
            total = total,
            items = vacations,
            offset = data.offset,
            limit = data.limit
        )
    }

    /**
     * 고정 휴가 상세 조회
     *
     * @param: id
     * @return id, memberId, vacationHistories, days, remainingDays, type, targetYear, createdAt
     */
    @GetMapping("/search/fixed/{id}")
    fun findFixedById(@PathVariable id: Long): FixedVacationDto {
        val authentication: AuthenticationDto = extractAuthenticationUseCase.extractAuthentication()
            ?: throw AuthenticationNotExistException()

        return findFixedVacationByIdUseCase.findById(authentication, id)
    }

    /**
     * 고정 휴가 생성 (추후 관리자가 수기 호출 가능 - 권한 설정)
     *
     * @param: memberId, targetYear?
     * @return id, memberId, vacationHistories, days, remainingDays, type, targetYear, createdAt
     */
    @PostMapping("/fixed")
    @ResponseStatus(HttpStatus.CREATED)
    fun createFixed(@RequestBody data: CreateVacationDto): FixedVacationDto {
        val authentication: AuthenticationDto = extractAuthenticationUseCase.extractAuthentication()
            ?: throw AuthenticationNotExistException()

        // TODO 권한 검증

        return createFixedVacationUseCase.create(
            memberId = data.memberId,
            targetYear = data.targetYear
        )
    }
}
