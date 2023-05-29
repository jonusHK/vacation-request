package com.bhkpo.vacation.domain.vacation

import com.bhkpo.vacation.common.code.VacationHistoryType
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

@Converter
class VacationTypeConverter : AttributeConverter<VacationHistoryType, String> {
    override fun convertToDatabaseColumn(attribute: VacationHistoryType): String {
        return attribute.code
    }

    override fun convertToEntityAttribute(dbData: String): VacationHistoryType {
        return VacationHistoryType.getByCode(dbData)
            ?: throw RuntimeException("휴가 유형 값이 올바르지 않습니다. (${dbData})")
    }
}
