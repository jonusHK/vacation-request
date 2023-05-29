package com.bhkpo.vacation.domain.vacation

import com.bhkpo.vacation.common.code.VacationType
import com.bhkpo.vacation.domain.auth.MemberEntity
import com.bhkpo.vacation.domain.vacationhistory.VacationHistoryEntity
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(
    name = "FixedVacation",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_target_year_member",
            columnNames = ["targetYear", "memberEmail"]
        )
    ]
)
@DiscriminatorValue("FIXED")
class FixedVacationEntity(

    member: MemberEntity,

    vacationHistories: MutableSet<VacationHistoryEntity> = mutableSetOf(),

    days: Float,

    remainingDays: Float,

    type: String,

    version: Long? = null,

    @Column
    val targetYear: Int,

    @Column
    var memberEmail: String = member.email

) : VacationEntity(
    member = member,
    vacationHistories = vacationHistories,
    days = days,
    remainingDays = remainingDays,
    type = type,
    version = version
) {

    companion object {
        const val DEFAULT_DAYS = 15.0F

        @JvmStatic
        fun new(
            member: MemberEntity,
            targetYear: Int? = null,
            days: Float? = null
        ): FixedVacationEntity {

            return FixedVacationEntity(
                member = member,
                type = VacationType.FIXED.code,
                days = days ?: DEFAULT_DAYS,
                remainingDays = days ?: DEFAULT_DAYS,
                targetYear = targetYear ?: defaultTargetYear
            )
        }

        @JvmStatic
        val defaultTargetYear: Int
            get() = LocalDateTime.now().year
    }
}
