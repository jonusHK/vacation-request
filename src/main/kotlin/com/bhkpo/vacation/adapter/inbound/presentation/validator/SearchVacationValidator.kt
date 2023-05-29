package com.bhkpo.vacation.adapter.inbound.presentation.validator

import com.bhkpo.vacation.adapter.inbound.presentation.dto.request.SearchFixedVacationDto
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext

class SearchVacationValidator : ConstraintValidator<ValidSearchVacation, SearchFixedVacationDto> {
    override fun isValid(value: SearchFixedVacationDto, context: ConstraintValidatorContext): Boolean {
        return true
    }
}
