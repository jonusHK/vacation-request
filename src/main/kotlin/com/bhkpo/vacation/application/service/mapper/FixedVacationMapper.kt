package com.bhkpo.vacation.application.service.mapper

import com.bhkpo.vacation.common.code.VacationType
import com.bhkpo.vacation.common.dto.FixedVacationDto
import com.bhkpo.vacation.domain.vacation.FixedVacationEntity

object FixedVacationMapper {

    fun toDto(entity: FixedVacationEntity): FixedVacationDto {

        return FixedVacationDto(
            id = entity.id!!,
            memberId = entity.member.id!!,
            vacationHistoryIds = entity.vacationHistories.map { it.id!! },
            days = entity.days,
            remainingDays = entity.remainingDays,
            type = VacationType.getByCode(entity.type)!!,
            targetYear = entity.targetYear,
            createdAt = entity.createdAt
        )
    }
}
