package com.bhkpo.vacation.domain.vacationhistory

import com.bhkpo.vacation.common.LocalDateTimeUtils.setMaxLocalTime
import com.bhkpo.vacation.common.LocalDateTimeUtils.setMinLocalTime
import com.bhkpo.vacation.common.LocalDateTimeUtils.setUntilMinutes
import com.bhkpo.vacation.common.code.VacationHistoryStatus
import com.bhkpo.vacation.common.code.VacationHistoryType
import com.bhkpo.vacation.common.exception.VacationHistoryAlreadyCanceledException
import com.bhkpo.vacation.common.exception.VacationHistoryAlreadyStartedException
import com.bhkpo.vacation.common.exception.VacationHistoryDaysNotExistException
import com.bhkpo.vacation.common.exception.VacationHistoryEndAtNotExistException
import com.bhkpo.vacation.domain.BaseEntity
import com.bhkpo.vacation.domain.vacation.VacationEntity
import com.bhkpo.vacation.domain.vacation.VacationTypeConverter
import jakarta.persistence.*
import org.hibernate.annotations.DynamicUpdate
import java.time.LocalDateTime

@Entity
@Table(name = "VacationHistory")
@DynamicUpdate
class VacationHistoryEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "vacation_history_id")
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vacation_id", foreignKey = ForeignKey(name = "fk_vacation_to_history"))
    var vacation: VacationEntity,

    @Convert(converter = VacationTypeConverter::class)
    @Column(nullable = false)
    val type: VacationHistoryType,

    @Column(nullable = false)
    val startAt: LocalDateTime,

    @Column(nullable = false)
    val endAt: LocalDateTime,

    @Column(nullable = false)
    val days: Float,

    @Convert(converter = VacationHistoryStatusConverter::class)
    @Column(nullable = false)
    var status: VacationHistoryStatus,

    @Column(length = 50)
    val comment: String? = null,

    @Column
    var canceledAt: LocalDateTime? = null

) : BaseEntity() {

    companion object {
        @JvmStatic
        fun new(
            vacation: VacationEntity,
            type: VacationHistoryType,
            dayHalf: Long,
            dayQuarter: Long,
            startAt: LocalDateTime,
            endAt: LocalDateTime? = null,
            days: Float? = null,
            comment: String? = null
        ): VacationHistoryEntity {

            // 휴가 신청 유형에 따라 휴가 기간 정보 추출
            val periodInfo: VacationHistoryPeriodDto = extractPeriodByType(
                type = type,
                startAt = startAt,
                endAt = endAt,
                dayHalf = dayHalf,
                dayQuarter = dayQuarter,
                days = days
            )

            // 휴가 신청기간에 따라 잔여일수 계산
            vacation.decreaseRemainingDays(periodInfo.days)

            return VacationHistoryEntity(
                vacation = vacation,
                type = type,
                startAt = periodInfo.startAt,
                endAt = periodInfo.endAt,
                days = periodInfo.days,
                comment = comment,
                status = VacationHistoryStatus.COMPLETED
            )
        }

        @JvmStatic
        private fun extractPeriodByType(
            type: VacationHistoryType,
            startAt: LocalDateTime,
            dayHalf: Long,
            dayQuarter: Long,
            endAt: LocalDateTime? = null,
            days: Float? = null
        ): VacationHistoryPeriodDto {
            return when (type) {
                // 휴가 신청 유형이 연차 인 경우
                VacationHistoryType.DAY -> {
                    if (endAt == null) throw VacationHistoryEndAtNotExistException()
                    if (days == null) throw VacationHistoryDaysNotExistException()
                    VacationHistoryPeriodDto(
                        days = days,
                        startAt = startAt.setMinLocalTime(),
                        endAt = endAt.setMaxLocalTime()
                    )
                }
                // 휴가 신청 유형이 반차 인 경우
                VacationHistoryType.HALF -> {
                    VacationHistoryPeriodDto(
                        days = VacationHistoryType.HALF.value,
                        startAt = startAt.setUntilMinutes(),
                        endAt = startAt.plusHours(dayHalf).setUntilMinutes()
                    )
                }
                // 휴가 신청 유형이 반반차 인 경우
                VacationHistoryType.QUARTER -> {
                    VacationHistoryPeriodDto(
                        days = VacationHistoryType.QUARTER.value,
                        startAt = startAt.setUntilMinutes(),
                        endAt = startAt.plusHours(dayQuarter).setUntilMinutes()
                    )
                }
            }
        }
    }

    fun cancel() {
        val now = LocalDateTime.now()
        var exception: RuntimeException? = null

        // 휴가 시작일보다 늦게 취소한 경우
        if (this.startAt <= now) {
            exception = VacationHistoryAlreadyStartedException()
        }
        // 이미 취소 상태인 경우
        if (this.status == VacationHistoryStatus.CANCELED) {
            exception = VacationHistoryAlreadyCanceledException()
        }
        if (exception != null) throw exception

        // 휴가 신청 취소
        status = VacationHistoryStatus.CANCELED
        canceledAt = now
        // 휴가 잔여일수 증가
        vacation.increaseRemainingDays(days)
    }
}
