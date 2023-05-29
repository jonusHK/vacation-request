package com.bhkpo.vacation.domain.vacation

import com.bhkpo.vacation.common.exception.VacationRemainingDaysLackException
import com.bhkpo.vacation.domain.BaseEntity
import com.bhkpo.vacation.domain.auth.MemberEntity
import com.bhkpo.vacation.domain.vacationhistory.VacationHistoryEntity
import jakarta.persistence.*
import org.hibernate.annotations.DynamicUpdate

@Entity
@Table(name = "Vacation")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "type")
@DynamicUpdate
class VacationEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "vacation_id")
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id")
    val member: MemberEntity,

    @OneToMany(mappedBy = "vacation", fetch = FetchType.LAZY, cascade = [CascadeType.PERSIST])
    val vacationHistories: MutableSet<VacationHistoryEntity> = mutableSetOf(),

    @Column
    val days: Float,

    @Column
    var remainingDays: Float,

    @Version
    val version: Long? = null,

    @Column(insertable = false, updatable = false)
    val type: String
) : BaseEntity() {

    /** 잔여 일수 차감 : 휴가 신청 시 호출 */
    fun decreaseRemainingDays(days: Float) {
        if (days > remainingDays) throw VacationRemainingDaysLackException()
        remainingDays -= days
        if (remainingDays < 0) remainingDays = 0.0F
    }

    /** 잔여 일수 증가 : 휴가 신청 취소 시 호출 */
    fun increaseRemainingDays(days: Float) {
        remainingDays += days
        if (remainingDays > this.days) remainingDays = this.days
    }
}
