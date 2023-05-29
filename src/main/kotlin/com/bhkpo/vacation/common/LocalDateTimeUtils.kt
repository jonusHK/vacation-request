package com.bhkpo.vacation.common

import java.time.LocalDateTime
import java.time.LocalTime

object LocalDateTimeUtils {

    // ex. 2023-04-30T00:00:00
    fun LocalDateTime.setMinLocalTime(): LocalDateTime {
        return this.with(LocalTime.MIN)
    }

    // ex. 2023-04-30T23:59:59
    fun LocalDateTime.setMaxLocalTime(): LocalDateTime {
        return this.with(LocalTime.MAX).withNano(0)
    }

    // ex. 2023-04-30T10:15:00, 2023-04-30T10:21:00
    fun LocalDateTime.setUntilMinutes(): LocalDateTime {
        return this.withSecond(0).withNano(0)
    }
}
