package com.bhkpo.vacation

import com.bhkpo.vacation.common.WorkingHourProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@EnableConfigurationProperties(WorkingHourProperties::class)
@SpringBootApplication
class VacationRequestApplication

fun main(args: Array<String>) {
    runApplication<VacationRequestApplication>(*args)
}
