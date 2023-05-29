package com.bhkpo.vacation.application.port.outbound.auth

interface PasswordEncodePort {

    fun encode(password: String): String
}
