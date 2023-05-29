package com.bhkpo.vacation.adapter.outbound.security

import io.jsonwebtoken.*
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import io.jsonwebtoken.security.SecurityException
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import java.security.Key
import java.util.*


class JwtTokenProvider(
    private val secret: String,
    private val tokenValidityInSeconds: Long
) {

    private val log = LoggerFactory.getLogger(this.javaClass)

    private var key: Key
    private val tokenValidityInMillis: Long = this.tokenValidityInSeconds * 1000

    companion object {
        const val AUTHORITIES_KEY = "auth"
        const val AUTHORITY_SEPARATOR = ","
    }

    init {
        val keyBytes: ByteArray = Decoders.BASE64.decode(this.secret)
        key = Keys.hmacShaKeyFor(keyBytes)
    }

    fun createToken(authentication: Authentication): String {

        val authorities: String = authentication.authorities.joinToString(AUTHORITY_SEPARATOR) {
            it.authority
        }
        val now = Date().time
        val validity = Date(now + tokenValidityInMillis)

        return Jwts.builder()
            .setSubject(authentication.name)
            .claim(AUTHORITIES_KEY, authorities)
            .signWith(key, SignatureAlgorithm.HS512)
            .setExpiration(validity)
            .compact()
    }

    fun getAuthentication(token: String): Authentication {

        val claims: Claims = getClaims(token)
        val authorities: List<SimpleGrantedAuthority> = claims[AUTHORITIES_KEY].toString()
            .split(AUTHORITY_SEPARATOR)
            .toList()
            .map { SimpleGrantedAuthority(it) }
        val principal = User(claims.subject, "", authorities)

        return UsernamePasswordAuthenticationToken(principal, token, authorities)
    }

    fun validateToken(token: String): Boolean {

        try {
            getClaims(token)
            return true
        } catch (e: SecurityException) {
            log.debug("잘못된 JWT 서명입니다.")
        } catch (e: MalformedJwtException) {
            log.debug("잘못된 JWT 서명입니다.")
        } catch (e: ExpiredJwtException) {
            log.debug("만료된 JWT 토큰입니다.")
        } catch (e: UnsupportedJwtException) {
            log.debug("지원되지 않는 JWT 토큰입니다.")
        } catch (e: IllegalArgumentException) {
            log.debug("JWT 토큰이 올바르지 않습니다.")
        }
        return false
    }

    private fun getClaims(token: String): Claims {

        return Jwts.parserBuilder()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token)
            .body
    }
}
