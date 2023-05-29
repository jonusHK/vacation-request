package com.bhkpo.vacation.domain.auth

import com.bhkpo.vacation.domain.BaseEntity
import com.bhkpo.vacation.domain.vacation.VacationEntity
import jakarta.persistence.*
import jakarta.validation.constraints.NotBlank
import org.hibernate.annotations.DynamicUpdate
import java.time.LocalDateTime

@Entity
@Table(name = "Member")
@DynamicUpdate
class MemberEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    var id: Long? = null,

    @OneToMany(mappedBy = "member", fetch = FetchType.LAZY, cascade = [CascadeType.ALL], orphanRemoval = true)
    val vacations: MutableSet<VacationEntity> = mutableSetOf(),

    @Column(unique = true, length = 50)
    val email: String,

    @Column(length = 100)
    @NotBlank
    val password: String,

    @Column
    var lastLoginAt: LocalDateTime? = null

) : BaseEntity() {
    fun login() {
        lastLoginAt = LocalDateTime.now()
    }
}
