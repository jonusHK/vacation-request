package com.bhkpo.vacation

import com.bhkpo.vacation.common.LocalDateTimeUtils.setMaxLocalTime
import com.bhkpo.vacation.common.LocalDateTimeUtils.setMinLocalTime
import com.bhkpo.vacation.common.LocalDateTimeUtils.setUntilMinutes
import com.bhkpo.vacation.common.code.VacationHistoryStatus
import com.bhkpo.vacation.common.code.VacationHistoryType
import com.bhkpo.vacation.common.code.VacationType
import com.bhkpo.vacation.domain.auth.MemberEntity
import com.bhkpo.vacation.domain.vacation.FixedVacationEntity
import com.bhkpo.vacation.domain.vacation.VacationEntity
import com.bhkpo.vacation.domain.vacationhistory.VacationHistoryEntity
import io.kotest.core.config.AbstractProjectConfig
import io.kotest.extensions.spring.SpringTestExtension
import io.kotest.extensions.spring.SpringTestLifecycleMode
import io.kotest.matchers.shouldBe
import jakarta.persistence.EntityManager
import java.time.LocalDateTime
import java.util.*
import java.util.concurrent.atomic.AtomicLong

class ProjectConfig : AbstractProjectConfig() {
    override fun extensions() = listOf(SpringTestExtension(SpringTestLifecycleMode.Root))


    companion object {

        private val uniqueEmail: String
            get() = "${UUID.randomUUID()}@test.com"

        private var memberIdx = AtomicLong(1L)

        private var vacationIdx = AtomicLong(1L)

        private var vacationHistoryIdx = AtomicLong(1L)

        fun createMember(
            email: String? = null,
            password: String = "test"
        ): MemberEntity {
            return MemberEntity(
                id = memberIdx.getAndIncrement(),
                email = email ?: uniqueEmail,
                password = password
            )
        }

        fun createFixedVacation(member: MemberEntity, targetYear: Int? = null): FixedVacationEntity {
            return FixedVacationEntity.new(member, targetYear).apply {
                id = vacationIdx.getAndIncrement()
                createdAt = LocalDateTime.now()
            }
        }

        fun createVacation(
            member: MemberEntity,
            type: VacationType = VacationType.FIXED,
            days: Float = FixedVacationEntity.DEFAULT_DAYS,
            remainingDays: Float = FixedVacationEntity.DEFAULT_DAYS
        ): VacationEntity {
            return VacationEntity(
                id = vacationIdx.getAndIncrement(),
                type = type.code,
                days = days,
                remainingDays = remainingDays,
                member = member
            ).apply {
                createdAt = LocalDateTime.now()
            }
        }

        fun createVacationHistory(
            vacation: VacationEntity,
            type: VacationHistoryType,
            dayHalf: Long = 4L,
            dayQuarter: Long = 2L,
            startAt: LocalDateTime = LocalDateTime.now(),
            endAt: LocalDateTime? = null,
            days: Float? = null,
            comment: String? = null
        ): VacationHistoryEntity {

            return VacationHistoryEntity.new(
                vacation = vacation,
                type = type,
                dayHalf = dayHalf,
                dayQuarter = dayQuarter,
                startAt = startAt,
                endAt = endAt,
                days = days,
                comment = comment
            ).apply {
                id = vacationHistoryIdx.getAndIncrement()
                createdAt = LocalDateTime.now()
            }
        }

        fun createMemberPersist(
            em: EntityManager,
            email: String? = null,
            password: String = "test"
        ): MemberEntity {

            val uniqueEmail = email ?: uniqueEmail
            val member = MemberEntity(email = uniqueEmail, password = password)

            em.persist(member)
            em.flush()
            em.clear()

            return member
        }

        fun createVacationPersist(
            em: EntityManager,
            type: VacationType,
            member: MemberEntity? = null,
            days: Float? = null,
            targetYear: Int? = null,
        ): VacationEntity {
            if (member == null) createMemberPersist(em)

            val vacation: VacationEntity = when (type) {
                VacationType.FIXED -> {
                    FixedVacationEntity.new(
                        member = member!!,
                        targetYear = targetYear,
                        days = days
                    )
                }
            }
            em.persist(vacation)
            em.flush()
            em.clear()

            return vacation
        }

        fun VacationHistoryEntity.validateVacationHistory(
            startAt: LocalDateTime,
            endAt: LocalDateTime? = null,
            days: Float? = null,
            dayHalf: Long,
            dayQuarter: Long,
            vacation: VacationEntity? = null,
            member: MemberEntity? = null,
            type: VacationType? = null,
            status: VacationHistoryStatus? = null,
            comment: String? = null
        ) {

            vacation?.let { this.vacation shouldBe it }
            member?.let { this.vacation.member shouldBe it }
            type?.let { this.type shouldBe it }
            status?.let { this.status shouldBe it }
            comment?.let { this.comment shouldBe it }

            when (this.type) {
                VacationHistoryType.DAY -> {
                    this.startAt shouldBe startAt.setMinLocalTime()
                    this.endAt shouldBe endAt!!.setMaxLocalTime()
                    this.days shouldBe days!!
                }
                VacationHistoryType.HALF -> {
                    this.startAt shouldBe startAt.setUntilMinutes()
                    this.endAt shouldBe startAt.plusHours(dayHalf).setUntilMinutes()
                    this.days shouldBe VacationHistoryType.HALF.value
                }
                VacationHistoryType.QUARTER -> {
                    this.startAt shouldBe startAt.setUntilMinutes()
                    this.endAt shouldBe startAt.plusHours(dayQuarter).setUntilMinutes()
                    this.days shouldBe VacationHistoryType.QUARTER.value
                }
            }
        }
    }
}
