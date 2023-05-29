package com.bhkpo.vacation.domain

import org.springframework.data.repository.NoRepositoryBean

@NoRepositoryBean
interface BaseQueryRepository<T, C, ID> {

    fun findById(id: ID): T?

    fun countByCondition(condition: C): ID

    fun searchByCondition(condition: C, offset: Long, limit: Long): List<T>?

    fun existsByCondition(condition: C): Boolean
}
