package com.bhkpo.vacation.application.port.inbound.auth

import com.bhkpo.vacation.common.dto.MemberDto

interface LoginMemberUseCase {

    fun login(email: String, password: String): MemberDto
}

