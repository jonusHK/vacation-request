package com.bhkpo.vacation.adapter

import com.bhkpo.vacation.adapter.ResponseCode.SuccessCode
import com.bhkpo.vacation.adapter.inbound.presentation.PageInfo
import com.bhkpo.vacation.adapter.inbound.presentation.dto.request.AuthLoginDto
import com.bhkpo.vacation.adapter.inbound.presentation.dto.request.AuthSignupDto
import com.bhkpo.vacation.common.code.VacationType
import com.bhkpo.vacation.domain.vacation.*
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.restassured.RestAssured
import io.restassured.response.ExtractableResponse
import io.restassured.response.Response
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class AuthApiTest(
    @LocalServerPort port: Int,
    @Autowired private val fixedVacationRepository: FixedVacationRepository
) : BehaviorSpec({

    beforeContainer {
        RestAssured.port = port
    }

    given("authentication") {
        val email = "test@test.com"
        val password = "test"
        val signupDto = AuthSignupDto(email = email, password = password)
        val loginDto = AuthLoginDto(email = email, password = password)

        `when`("회원가입, 로그인 API 를 호출하면") {
            val signupResponse: ExtractableResponse<Response> = RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(signupDto)
                .`when`()
                .post("/api/v1/auth/signup")
                .then()
                .log().all().extract()

            then("정상적으로 회원가입이 완료된다.") {
                signupResponse.statusCode() shouldBe HttpStatus.CREATED.value()
                signupResponse.jsonPath().getString("code") shouldBe SuccessCode.CREATED.code
                signupResponse.jsonPath().getString("data.id") shouldNotBe null
                signupResponse.jsonPath().getString("data.email") shouldBe signupDto.email
            }

            then("15일의 고정 휴가가 생성된다.") {
                // 회원 가입 시 생성된 휴가 조회
                val vacations: List<FixedVacationEntity>? = fixedVacationRepository.searchByCondition(
                    FixedVacationSearchCondition(email),
                    PageInfo.OFFSET,
                    PageInfo.LIMIT
                )
                val vacation: FixedVacationEntity? = vacations?.first()

                vacation shouldNotBe null
                vacation!!.targetYear shouldBe FixedVacationEntity.defaultTargetYear
                vacation.days shouldBe FixedVacationEntity.DEFAULT_DAYS
                vacation.remainingDays shouldBe FixedVacationEntity.DEFAULT_DAYS
                vacation.type shouldBe VacationType.FIXED.code
                vacation.member.id shouldBe signupResponse.jsonPath().getString("data.id").toLong()
            }


            val loginResponse: ExtractableResponse<Response> = RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(loginDto)
                .`when`()
                .post("/api/v1/auth/login")
                .then()
                .log().all().extract()

            then("정상적으로 로그인 된다.") {
                loginResponse.statusCode() shouldBe HttpStatus.OK.value()
                loginResponse.jsonPath().getString("code") shouldBe SuccessCode.OK.code
                loginResponse.jsonPath().getString("data.email") shouldBe email
                val accessToken = loginResponse.jsonPath().getString("data.accessToken")
                accessToken shouldNotBe null
                loginResponse.header("Authorization") shouldBe "Bearer $accessToken"
            }
        }
    }
})
