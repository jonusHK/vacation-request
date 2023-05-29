package com.bhkpo.vacation.common

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("working.hour")
class WorkingHourProperties {

    var day: Long = 0L

    var half: Long = 0L

    var quarter: Long = 0L
}
