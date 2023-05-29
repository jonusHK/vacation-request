package com.bhkpo.vacation.domain

import com.bhkpo.vacation.ProjectConfig.Companion.createMemberPersist
import com.bhkpo.vacation.adapter.inbound.presentation.PageInfo
import com.bhkpo.vacation.common.code.VacationType
import com.bhkpo.vacation.domain.auth.MemberEntity
import com.bhkpo.vacation.domain.vacation.*
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import jakarta.persistence.EntityManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Transactional
internal class FixedVacationRepositoryTest @Autowired constructor(
    private val em: EntityManager,
    private val fixedVacationRepository: FixedVacationRepository
) : BehaviorSpec({

    given("save") {
        val member: MemberEntity = createMemberPersist(em)
        `when`("고정 휴가 생성하면") {
            val fixedVacation: FixedVacationEntity = FixedVacationEntity.new(member)
            fixedVacationRepository.save(fixedVacation)
            then("데이터가 정상 생성된다.") {
                verifyNewEntity(fixedVacation, member)
            }
        }
    }

    given("findById") {
        val member: MemberEntity = createMemberPersist(em)
        val newEntity: FixedVacationEntity = FixedVacationEntity.new(member)
        fixedVacationRepository.save(newEntity)
        `when`("ID로 휴가를 조회하면") {
            val fixedVacation: FixedVacationEntity? = fixedVacationRepository.findById(newEntity.id!!)
            then("데이터가 정상 조회된다.") {
                fixedVacation shouldNotBe null
                verifyNewEntity(fixedVacation!!, member)
            }
        }
    }
    
    given("countByCondition") {
        val count = 5
        var targetYear: Int = FixedVacationEntity.defaultTargetYear
        val member: MemberEntity = createMemberPersist(em)
        val member2 = createMemberPersist(em, "test2@test.com", "test")
        (1..count).forEach { i ->
            if (i == count) {
                fixedVacationRepository.save(
                    FixedVacationEntity.new(member2, targetYear)
                )
            } else {
                fixedVacationRepository.save(
                    FixedVacationEntity.new(member, targetYear++)
                )
            }
        }
        `when`("memberId 를 조건으로 필터하면") {
            val condition = FixedVacationSearchCondition(memberEmail = member.email)
            val vacationCnt: Long = fixedVacationRepository.countByCondition(condition)
            then("필터된 총 데이터 수를 보여준다.") {
                vacationCnt shouldBe count - 1
            }
        }
        `when`("memberId, targetYear 를 조건으로 필터하면") {
            val condition = FixedVacationSearchCondition(memberEmail = member2.email, targetYear = targetYear)
            val vacationCnt: Long = fixedVacationRepository.countByCondition(condition)
            then("필터된 총 데이터 수를 보여준다.") {
                vacationCnt shouldBe 1
            }
        }
    }

    given("searchByCondition") {
        val count = 5
        var targetYear: Int = FixedVacationEntity.defaultTargetYear
        val member: MemberEntity = createMemberPersist(em)
        val member2 = MemberEntity(email = "test2@test.com", password = "test")
        em.persist(member2)
        var offset = PageInfo.OFFSET
        var limit = PageInfo.LIMIT
        (1..count).forEach { i ->
            if (i == count) {
                fixedVacationRepository.save(FixedVacationEntity.new(member2, targetYear))
            } else {
                fixedVacationRepository.save(FixedVacationEntity.new(member, targetYear++))
            }
        }
        `when`("memberId 를 조건으로 필터하면") {
            val condition = FixedVacationSearchCondition(memberEmail = member.email)
            val fixedVacations: List<FixedVacationEntity>? = fixedVacationRepository.searchByCondition(condition, offset, limit)
            then("데이터가 필터되어 조회된다.") {
                fixedVacations shouldNotBe null
                fixedVacations!!.size shouldBe count - 1
                fixedVacations.forEach {
                    verifyNewEntity(it, member, it.targetYear)
                }
            }
        }
        `when`("memberId, targetYear 를 조건으로 필터하면") {
            val condition = FixedVacationSearchCondition(memberEmail = member2.email, targetYear = targetYear)
            val fixedVacations: List<FixedVacationEntity>? = fixedVacationRepository.searchByCondition(condition, offset, limit)
            then("데이터가 필터되어 조회된다.") {
                fixedVacations shouldNotBe null
                fixedVacations!!.size shouldBe 1
                fixedVacations.forEach {
                    verifyNewEntity(it, member2, targetYear)
                }
            }
        }
        `when`("offset, limit 을 적용하여 조회하면") {
            offset = 1
            limit = 3
            val condition = FixedVacationSearchCondition(memberEmail = member.email)
            val fixedVacations: List<FixedVacationEntity>? = fixedVacationRepository.searchByCondition(
                condition, offset, limit
            )
            then("해당된 데이터가 필터되어 조회된다.") {
                fixedVacations shouldNotBe null
                fixedVacations!!.size shouldBe limit
            }
        }
    }
}) {
    companion object {

        private fun verifyNewEntity(
            fixedVacation: FixedVacationEntity,
            member: MemberEntity,
            targetYear: Int? = null
        ) {
            fixedVacation.memberEmail shouldBe member.email
            fixedVacation.targetYear shouldBe (targetYear ?: FixedVacationEntity.defaultTargetYear)
            fixedVacation shouldNotBe null
            fixedVacation.member shouldBe member
            fixedVacation.days shouldBe FixedVacationEntity.DEFAULT_DAYS
            fixedVacation.remainingDays shouldBe FixedVacationEntity.DEFAULT_DAYS
            fixedVacation.type shouldBe VacationType.FIXED.code
            fixedVacation.vacationHistories.size shouldBe 0
        }
    }
}
