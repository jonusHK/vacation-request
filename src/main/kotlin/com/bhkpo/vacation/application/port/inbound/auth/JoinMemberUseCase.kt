package com.bhkpo.vacation.application.port.inbound.auth

interface JoinMemberUseCase {

    fun join(email: String, password: String): Long
}
