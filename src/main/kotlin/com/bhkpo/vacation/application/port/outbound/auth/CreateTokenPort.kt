package com.bhkpo.vacation.application.port.outbound.auth

interface CreateTokenPort {

    fun createToken(email: String, password: String): String
}
