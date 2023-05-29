package com.bhkpo.vacation.adapter.outbound.security

import com.bhkpo.vacation.domain.auth.MemberEntity
import com.bhkpo.vacation.domain.auth.MemberRepository
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Component

@Component
class SecurityUserDetailService(
    private val memberRepository: MemberRepository
) : UserDetailsService {

    override fun loadUserByUsername(email: String): UserDetails {
        val member: MemberEntity = memberRepository.findByEmail(email)
            ?: throw UsernameNotFoundException("해당 유저를 찾을 수 없습니다. email=$email")

        return createSecurityUser(member)
    }

    private fun createSecurityUser(member: MemberEntity): User {
        return User(
            member.email,
            member.password,
            listOf(SimpleGrantedAuthority("USER"))
        )
    }
}
