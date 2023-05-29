package com.bhkpo.vacation.domain.vacationhistory

import com.bhkpo.vacation.common.code.VacationHistoryStatus
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

@Converter
class VacationHistoryStatusConverter : AttributeConverter<VacationHistoryStatus, String> {
    override fun convertToDatabaseColumn(attribute: VacationHistoryStatus): String {
        return attribute.code
    }

    override fun convertToEntityAttribute(dbData: String): VacationHistoryStatus {
        return VacationHistoryStatus.getByCode(dbData)
            ?: throw RuntimeException("휴가 처리 상태 값이 올바르지 않습니다. ($dbData)")
    }
}
