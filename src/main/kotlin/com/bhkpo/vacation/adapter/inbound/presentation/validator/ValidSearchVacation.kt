package com.bhkpo.vacation.adapter.inbound.presentation.validator

import jakarta.validation.Constraint
import jakarta.validation.Payload
import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [SearchVacationValidator::class])
annotation class ValidSearchVacation(

    val message: String = "올바르지 않은 데이터 값입니다.",

    val groups: Array<KClass<*>> = [],

    val payload: Array<KClass<out Payload>> = []
)
