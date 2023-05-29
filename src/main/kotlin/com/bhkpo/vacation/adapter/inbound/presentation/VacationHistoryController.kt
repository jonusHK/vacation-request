package com.bhkpo.vacation.adapter.inbound.presentation

import com.bhkpo.vacation.adapter.inbound.presentation.dto.request.RequestVacationHistoryDto
import com.bhkpo.vacation.adapter.inbound.presentation.dto.request.SearchVacationHistoryDto
import com.bhkpo.vacation.adapter.inbound.presentation.dto.response.CollectionResponseDto
import com.bhkpo.vacation.application.port.inbound.auth.ExtractAuthenticationUseCase
import com.bhkpo.vacation.application.port.inbound.vacationhistory.CancelVacationHistoryUseCase
import com.bhkpo.vacation.application.port.inbound.vacationhistory.CountVacationHistoriesUseCase
import com.bhkpo.vacation.application.port.inbound.vacationhistory.CreateVacationHistoryUseCase
import com.bhkpo.vacation.application.port.inbound.vacationhistory.SearchVacationHistoriesUseCase
import com.bhkpo.vacation.common.dto.AuthenticationDto
import com.bhkpo.vacation.common.dto.CancelVacationHistoryDto
import com.bhkpo.vacation.common.dto.CreateVacationHistoryDto
import com.bhkpo.vacation.common.dto.VacationHistoryDto
import com.bhkpo.vacation.common.exception.AuthenticationNotExistException
import org.springframework.http.HttpStatus
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/vacation/history")
class VacationHistoryController(
    private val countVacationHistoriesUseCase: CountVacationHistoriesUseCase,
    private val searchVacationHistoriesUseCase: SearchVacationHistoriesUseCase,
    private val createVacationHistoryUseCase: CreateVacationHistoryUseCase,
    private val cancelVacationHistoryUseCase: CancelVacationHistoryUseCase,
    private val extractAuthenticationUseCase: ExtractAuthenticationUseCase
) {

    /**
     * 휴가 신청 (연차/반차/반반차)
     *
     * @param: vacationId, type, startAt, endAt?, days?, comment?
     * @return id, vacationId, remainingDays, type, startAt, endAt, days, status, comment
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@Validated @RequestBody data: RequestVacationHistoryDto): CreateVacationHistoryDto {

        // 인증 객체 조회
        val authentication: AuthenticationDto = extractAuthenticationUseCase.extractAuthentication()
            ?: throw AuthenticationNotExistException()

        // 휴가 내역 생성
        return createVacationHistoryUseCase.create(
            authentication = authentication,
            vacationId = data.vacationId,
            type = data.typeEnum,
            startAt = data.startAt,
            endAt = data.endAt,
            days = data.days?.toFloat(),
            comment = data.comment
        )
    }

    /**
     * 휴가 신청 리스트 조회
     *
     * @param: vacationId?, type?, status?, startAtLoe?, startAtGoe?, endAtLoe?, endAtGoe?, offset?, limit?
     * @return total, items, offset, limit
     */
    @GetMapping("/search")
    fun search(@Validated data: SearchVacationHistoryDto): CollectionResponseDto<VacationHistoryDto> {

        // 인증 객체 조회
        val authentication: AuthenticationDto = extractAuthenticationUseCase.extractAuthentication()
            ?: throw AuthenticationNotExistException()

        // 검색된 총 결과 수 조회
        val total: Long = countVacationHistoriesUseCase.count(
            authentication = authentication,
            vacationId = data.vacationId,
            type = data.typeEnum,
            status = data.statusEnum,
            startAtLoe = data.startAtLoe,
            startAtGoe = data.startAtGoe,
            endAtLoe = data.endAtLoe,
            endAtGoe = data.endAtGoe
        )

        // 검색 조건에 해당하는 휴가 신청 내역 목록 추출
        val vacationHistories: List<VacationHistoryDto> = searchVacationHistoriesUseCase.search(
            authentication = authentication,
            vacationId = data.vacationId,
            type = data.typeEnum,
            status = data.statusEnum,
            startAtLoe = data.startAtLoe,
            startAtGoe = data.startAtGoe,
            endAtLoe = data.endAtLoe,
            endAtGoe = data.endAtGoe,
            offset = data.offset,
            limit = data.limit
        )

        return CollectionResponseDto(
            total = total,
            items = vacationHistories,
            offset = data.offset,
            limit = data.limit
        )
    }

    /**
     * 휴가 신청 취소
     *
     * @param: id
     * @return id, vacationId, remainingDays, status, cancelAt
     */
    @PatchMapping("/cancel/{id}")
    fun cancel(@PathVariable id: Long): CancelVacationHistoryDto {
        val authentication: AuthenticationDto = extractAuthenticationUseCase.extractAuthentication()
            ?: throw AuthenticationNotExistException()

        return cancelVacationHistoryUseCase.cancel(authentication = authentication, id = id)
    }
}
