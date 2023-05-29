package com.bhkpo.vacation.adapter

import com.bhkpo.vacation.adapter.ResponseCode.SuccessCode.CREATED
import com.bhkpo.vacation.adapter.ResponseCode.SuccessCode.OK
import com.bhkpo.vacation.adapter.inbound.presentation.PageInfo
import com.bhkpo.vacation.adapter.inbound.presentation.dto.request.AuthLoginDto
import com.bhkpo.vacation.adapter.inbound.presentation.dto.request.AuthSignupDto
import com.bhkpo.vacation.adapter.inbound.presentation.dto.request.RequestVacationHistoryDto
import com.bhkpo.vacation.common.LocalDateTimeUtils.setMaxLocalTime
import com.bhkpo.vacation.common.LocalDateTimeUtils.setMinLocalTime
import com.bhkpo.vacation.common.LocalDateTimeUtils.setUntilMinutes
import com.bhkpo.vacation.common.WorkingHourProperties
import com.bhkpo.vacation.common.code.VacationHistoryStatus
import com.bhkpo.vacation.common.code.VacationHistoryType
import com.bhkpo.vacation.common.dto.MemberDto
import com.bhkpo.vacation.domain.vacation.VacationEntity
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
import java.time.LocalDateTime
import java.util.*

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class VacationHistoryApiTest(
    @LocalServerPort port: Int,
    @Autowired private val workingHourProperties: WorkingHourProperties,
    @Autowired private val vacationRepository: VacationRepository
) : DescribeSpec({

    beforeContainer {
        RestAssured.port = port
    }

    describe("연차 휴가") {
        context("연차 휴가를 신청하는 경우") {
            val days = 5
            val startAt = LocalDateTime.now().plusDays(1)
            val endAt = startAt.plusDays(days.toLong())
            val comment = "코멘트"

            // 회원 가입 및 로그인
            val memberDto: MemberDto = getMemberAfterSignupAndLogin()

            // 회원 가입 시 생성된 휴가 조회
            val vacations: List<VacationEntity>? = vacationRepository.searchByCondition(
                VacationSearchCondition(memberDto.email),
                PageInfo.OFFSET,
                PageInfo.LIMIT
            )
            val vacationId: Long? = vacations?.first()?.id

            // 연차 휴가 생성
            val vacationHistoryResponse = createVacationHistoryAndGetResponse(
                memberDto.accessToken,
                RequestVacationHistoryDto(
                    vacationId = vacationId!!,
                    type = VacationHistoryType.DAY.code,
                    startAt = startAt,
                    endAt = endAt,
                    days = days,
                    comment = comment
                )
            )

            it("데이터가 정상적으로 응답된다.") {

                // 반환 데이터 검증
                verifyCreatedVacationHistory(
                    response = vacationHistoryResponse,
                    type = VacationHistoryType.DAY.code,
                    dayHalf = workingHourProperties.half,
                    dayQuarter = workingHourProperties.quarter,
                    startAt = startAt,
                    endAt = endAt,
                    days = days.toFloat(),
                    comment = comment
                )
            }
        }
    }

    describe("반차 휴가") {
        context("반차 휴가를 신청하는 경우") {
            val startAt = LocalDateTime.now().plusDays(1)
            val comment = "코멘트"

            // 회원 가입 및 로그인
            val memberDto: MemberDto = getMemberAfterSignupAndLogin()

            // 회원 가입 시 생성된 휴가 조회
            val vacations: List<VacationEntity>? = vacationRepository.searchByCondition(
                VacationSearchCondition(memberDto.email),
                PageInfo.OFFSET,
                PageInfo.LIMIT
            )
            val vacationId: Long? = vacations?.first()?.id

            // 반차 휴가 생성
            val response: ExtractableResponse<Response> = createVacationHistoryAndGetResponse(
                memberDto.accessToken,
                RequestVacationHistoryDto(
                    vacationId = vacationId!!,
                    type = VacationHistoryType.HALF.code,
                    startAt = startAt,
                    comment = comment
                )
            )

            it("데이터가 정상적으로 응답된다.") {
                // 반환 데이터 검증
                verifyCreatedVacationHistory(
                    response = response,
                    type = VacationHistoryType.HALF.code,
                    dayHalf = workingHourProperties.half,
                    dayQuarter = workingHourProperties.quarter,
                    startAt = startAt,
                    comment = comment
                )
            }
        }
    }

    describe("반반차 휴가") {
        context("반반차 휴가를 신청하는 경우") {
            val startAt = LocalDateTime.now().plusDays(1)
            val comment = "코멘트"

            // 회원 가입 및 로그인
            val memberDto: MemberDto = getMemberAfterSignupAndLogin()

            // 회원 가입 시 생성된 휴가 조회
            val vacations: List<VacationEntity>? = vacationRepository.searchByCondition(
                VacationSearchCondition(memberDto.email),
                PageInfo.OFFSET,
                PageInfo.LIMIT
            )
            val vacationId: Long? = vacations?.first()?.id

            // 반반차 휴가 생성
            val response: ExtractableResponse<Response> = createVacationHistoryAndGetResponse(
                memberDto.accessToken,
                RequestVacationHistoryDto(
                    vacationId = vacationId!!,
                    type = VacationHistoryType.QUARTER.code,
                    startAt = startAt,
                    comment = comment
                )
            )

            it("데이터가 정상적으로 응답된다.") {
                // 반환 데이터 검증
                verifyCreatedVacationHistory(
                    response = response,
                    type = VacationHistoryType.QUARTER.code,
                    dayHalf = workingHourProperties.half,
                    dayQuarter = workingHourProperties.quarter,
                    startAt = startAt,
                    comment = comment
                )
            }
        }
    }

    describe("휴가 신청 내역 조회") {
        context("휴가 신청 리스트를 모두 조회하면") {

            // 회원 가입 및 로그인
            val memberDto: MemberDto = getMemberAfterSignupAndLogin()

            // 휴가 신청 내역 조회
            val response = RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .auth().oauth2(memberDto.accessToken)
                .`when`()
                .get("/api/v1/vacation/history/search")
                .then()
                .log().all().extract()

            it("정상적으로 데이터를 반환한다.") {
                response.statusCode() shouldBe HttpStatus.OK.value()
                response.jsonPath().getString("code") shouldBe OK.code
                response.jsonPath().getString("data") shouldNotBe null
                response.jsonPath().getString("data.total") shouldNotBe null
                response.jsonPath().getString("data.items") shouldNotBe null
                response.jsonPath().getString("data.offset") shouldNotBe null
                response.jsonPath().getString("data.limit") shouldNotBe null
            }
        }
    }

    describe("휴가 신청 취소") {
        context("특정 휴가 신청 내역을 취소한다면") {

            // 회원 가입 및 로그인
            val memberDto: MemberDto = getMemberAfterSignupAndLogin()

            // 회원 가입 시 생성된 휴가 조회
            val vacations: List<VacationEntity>? = vacationRepository.searchByCondition(
                VacationSearchCondition(memberDto.email),
                PageInfo.OFFSET,
                PageInfo.LIMIT
            )
            val vacationId: Long? = vacations?.first()?.id

            // 휴가 신청 내역 생성
            val startAt = LocalDateTime.now().plusDays(1)
            val comment = "코멘트"
            val createdResponse = createVacationHistoryAndGetResponse(
                memberDto.accessToken,
                RequestVacationHistoryDto(
                    vacationId = vacationId!!,
                    type = VacationHistoryType.HALF.code,
                    startAt = startAt,
                    comment = comment
                )
            )
            createdResponse.statusCode() shouldBe HttpStatus.CREATED.value()

            // 신청 내역 취소
            val cancelHistoryId: Long = createdResponse.jsonPath().getString("data.id").toLong()
            val canceledResponse = RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .auth().oauth2(memberDto.accessToken)
                .`when`()
                .patch("/api/v1/vacation/history/cancel/$cancelHistoryId")
                .then()
                .log().all().extract()

            it("정상적으로 취소되어 데이터가 반환된다.") {
                canceledResponse.statusCode() shouldBe HttpStatus.OK.value()
                canceledResponse.jsonPath().getString("code") shouldBe OK.code
                canceledResponse.jsonPath().getString("data") shouldNotBe null
                canceledResponse.jsonPath().getString("data.id").toInt() shouldBe cancelHistoryId
                canceledResponse.jsonPath().getString("data.status") shouldBe VacationHistoryStatus.CANCELED.code
            }
        }
    }
}) {

    companion object {
        fun getMemberAfterSignupAndLogin(email: String? = null, password: String = "test"): MemberDto {
            val uuidEmail = email ?: "${UUID.randomUUID()}@test.com"
            val signupDto = AuthSignupDto(email = uuidEmail, password = password)
            val loginDto = AuthLoginDto(email = uuidEmail, password = password)

            // 회원 가입
            RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(signupDto)
                .`when`()
                .post("/api/v1/auth/signup")
                .then()
                .log().all().extract()

            // 로그인
            val loginResponse = RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(loginDto)
                .`when`()
                .post("/api/v1/auth/login")
                .then()
                .log().all().extract()

            return MemberDto(
                id = loginResponse.jsonPath().getString("data.id").toLong(),
                email = loginResponse.jsonPath().getString("data.email"),
                accessToken = loginResponse.jsonPath().getString("data.accessToken")
            )
        }

        fun createVacationHistoryAndGetResponse(
            accessToken: String,
            data: RequestVacationHistoryDto
        ): ExtractableResponse<Response> {
            return RestAssured.given().log().all()
                .auth().oauth2(accessToken)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(data)
                .`when`()
                .post("/api/v1/vacation/history")
                .then()
                .log().all().extract()
        }

        fun verifyCreatedVacationHistory(
            response: ExtractableResponse<Response>,
            type: String,
            dayHalf: Long,
            dayQuarter: Long,
            startAt: LocalDateTime,
            endAt: LocalDateTime? = null,
            days: Float? = null,
            comment: String? = null
        ) {
            response.statusCode() shouldBe HttpStatus.CREATED.value()
            response.jsonPath().getString("code") shouldBe CREATED.code
            response.jsonPath().getString("data") shouldNotBe null
            response.jsonPath().getString("data.status") shouldBe VacationHistoryStatus.COMPLETED.code
            response.jsonPath().getString("data.type") shouldBe type

            comment?.let {
                response.jsonPath().getString("data.comment") shouldBe comment
            }

            when (type) {
                VacationHistoryType.DAY.code -> {
                    LocalDateTime.parse(response.jsonPath().getString("data.startAt")) shouldBe startAt.setMinLocalTime()
                    LocalDateTime.parse(response.jsonPath().getString("data.endAt")) shouldBe endAt!!.setMaxLocalTime()
                    response.jsonPath().getString("data.days").toFloat() shouldBe days!!
                }
                VacationHistoryType.HALF.code -> {
                    val startAtUntilMinutes = startAt.setUntilMinutes()
                    LocalDateTime.parse(response.jsonPath().getString("data.startAt")) shouldBe startAtUntilMinutes
                    LocalDateTime.parse(response.jsonPath().getString("data.endAt")) shouldBe startAtUntilMinutes.plusHours(dayHalf)
                    response.jsonPath().getString("data.days").toFloat() shouldBe VacationHistoryType.HALF.value
                }
                VacationHistoryType.QUARTER.code -> {
                    val startAtUntilMinutes = startAt.setUntilMinutes()
                    LocalDateTime.parse(response.jsonPath().getString("data.startAt")) shouldBe startAtUntilMinutes
                    LocalDateTime.parse(response.jsonPath().getString("data.endAt")) shouldBe startAtUntilMinutes.plusHours(dayQuarter)
                    response.jsonPath().getString("data.days").toFloat() shouldBe VacationHistoryType.QUARTER.value
                }
            }
        }
    }
}