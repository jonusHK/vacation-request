package com.bhkpo.vacation.adapter

import com.bhkpo.vacation.adapter.ResponseCode.SuccessCode.*
import com.bhkpo.vacation.adapter.inbound.presentation.PageInfo
import com.bhkpo.vacation.adapter.inbound.presentation.dto.CreateVacationDto
import com.bhkpo.vacation.adapter.inbound.presentation.dto.request.AuthLoginDto
import com.bhkpo.vacation.adapter.inbound.presentation.dto.request.AuthSignupDto
import com.bhkpo.vacation.common.code.VacationType
import com.bhkpo.vacation.common.dto.MemberDto
import com.bhkpo.vacation.domain.vacation.FixedVacationEntity
import com.bhkpo.vacation.domain.vacation.VacationRepository
import com.bhkpo.vacation.domain.vacation.VacationSearchCondition
import io.kotest.core.spec.style.DescribeSpec
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
import java.util.*

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class VacationApiTest(
    @LocalServerPort port: Int,
    @Autowired private val vacationRepository: VacationRepository
) : DescribeSpec({

    beforeContainer {
        RestAssured.port = port
    }

    describe("고정 휴가 조회") {
        val memberDto: MemberDto = getMemberAfterSignupAndLogin()

        context("고정 휴가를 모두 조회하면") {
            val searchFixedResponse: ExtractableResponse<Response> = RestAssured.given().log().all()
                .auth().oauth2(memberDto.accessToken)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .`when`()
                .get("/api/v1/vacation/search/fixed")
                .then()
                .log().all().extract()

            it("리스트 데이터 형식으로 정상적으로 조회되는지 확인한다.") {
                searchFixedResponse.statusCode() shouldBe HttpStatus.OK.value()
                searchFixedResponse.jsonPath().getString("data") shouldNotBe null
                searchFixedResponse.jsonPath().getString("data.total").toInt() shouldBe 1
                searchFixedResponse.jsonPath().getString("data.items") shouldNotBe null
                searchFixedResponse.jsonPath().getString("data.offset").toLong() shouldBe PageInfo.OFFSET
                searchFixedResponse.jsonPath().getString("data.limit").toLong() shouldBe PageInfo.LIMIT
            }
        }
        context("휴가 상세 데이터를 조회하면") {
            val vacationId: Long? = vacationRepository.searchByCondition(
                VacationSearchCondition(memberEmail = memberDto.email),
                PageInfo.OFFSET,
                PageInfo.LIMIT
            )?.first()?.id

            it("해당 데이터가 정상적으로 조회된다.") {
                val findFixedResponse: ExtractableResponse<Response> = RestAssured.given().log().all()
                    .auth().oauth2(memberDto.accessToken)
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .`when`()
                    .get("/api/v1/vacation/search/fixed/$vacationId")
                    .then()
                    .log().all().extract()

                findFixedResponse.statusCode() shouldBe HttpStatus.OK.value()
                findFixedResponse.jsonPath().getString("data") shouldNotBe null
                findFixedResponse.jsonPath().getString("data.id").toLong() shouldBe vacationId
                findFixedResponse.jsonPath().getString("data.memberId").toLong() shouldBe memberDto.id
                findFixedResponse.jsonPath().getString("data.targetYear").toInt() shouldBe FixedVacationEntity.defaultTargetYear
                findFixedResponse.jsonPath().getString("data.days").toFloat() shouldBe FixedVacationEntity.DEFAULT_DAYS
                findFixedResponse.jsonPath().getString("data.type") shouldBe VacationType.FIXED.code
            }
        }
    }

    describe("고정 휴가 생성") {
        context("고정 휴가를 생성하면") {
            it("생성 이후 데이터가 정상 반환된다.") {

                // 회원 가입 및 로그인
                val memberDto: MemberDto = getMemberAfterSignupAndLogin()

                // 휴가 생성
                val targetYear: Int = FixedVacationEntity.defaultTargetYear + 1
                val data = CreateVacationDto(
                    memberId = memberDto.id,
                    targetYear = targetYear
                )
                val response = RestAssured.given().log().all()
                    .auth().oauth2(memberDto.accessToken)
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .`when`()
                    .body(data)
                    .post("/api/v1/vacation/fixed")
                    .then()
                    .log().all().extract()

                response.statusCode() shouldBe HttpStatus.CREATED.value()
                response.jsonPath().getString("code") shouldBe CREATED.code
                response.jsonPath().getString("data") shouldNotBe null
                response.jsonPath().getString("data.id") shouldNotBe null
                response.jsonPath().getString("data.memberId").toLong() shouldBe memberDto.id
                response.jsonPath().getString("data.vacationHistoryIds") shouldNotBe null
                response.jsonPath().getString("data.targetYear").toInt() shouldBe targetYear
            }
        }
    }

}) {
    companion object {
        fun getMemberAfterSignupAndLogin(email: String? = null, password: String = "test"): MemberDto {
            val uuidEmail = email ?: "${UUID.randomUUID()}@test.com"

            val signupDto = AuthSignupDto(email = uuidEmail, password = password)
            val loginDto = AuthLoginDto(email = uuidEmail, password = password)

            RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(signupDto)
                .`when`()
                .post("/api/v1/auth/signup")
                .then()
                .log().all().extract()

            val loginResponse: ExtractableResponse<Response> = RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(loginDto)
                .`when`()
                .post("/api/v1/auth/login")
                .then()
                .log().all().extract()

            return MemberDto(
                id = loginResponse.jsonPath().getString("data.id").toLong(),
                email = loginResponse.jsonPath().getString("data.email"),
                accessToken = loginResponse.jsonPath().getString("data.accessToken"),
            )
        }
    }
}