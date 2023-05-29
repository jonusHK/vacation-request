package com.bhkpo.vacation.application.service.mapper

import com.bhkpo.vacation.common.dto.CancelVacationHistoryDto
import com.bhkpo.vacation.common.dto.CreateVacationHistoryDto
import com.bhkpo.vacation.common.dto.VacationHistoryDto
import com.bhkpo.vacation.domain.vacationhistory.VacationHistoryEntity

object VacationHistoryMapper {

    fun toDto(entity: VacationHistoryEntity): VacationHistoryDto {

        return VacationHistoryDto(
            id = entity.id!!,
            vacationId = entity.vacation.id!!,
            type = entity.type,
            startAt = entity.startAt,
            endAt = entity.endAt,
            days = entity.days,
            status = entity.status,
            comment = entity.comment,
            canceledAt = entity.canceledAt,
            createdAt = entity.createdAt
        )
    }

    fun toCreateDto(entity: VacationHistoryEntity): CreateVacationHistoryDto {

        return CreateVacationHistoryDto(
            id = entity.id!!,
            vacationId = entity.vacation.id!!,
            remainingDays = entity.vacation.remainingDays,
            type = entity.type,
            startAt = entity.startAt,
            endAt = entity.endAt,
            days = entity.days,
            status = entity.status,
            comment = entity.comment
        )
    }

    fun toCancelDto(entity: VacationHistoryEntity): CancelVacationHistoryDto {

        return CancelVacationHistoryDto(
            id = entity.id!!,
            vacationId = entity.vacation.id!!,
            remainingDays = entity.vacation.remainingDays,
            status = entity.status,
            canceledAt = entity.canceledAt!!
        )
    }
}
